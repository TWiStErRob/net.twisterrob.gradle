package net.twisterrob.gradle.android

import com.android.build.api.component.impl.AndroidTestImpl
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.impl.VariantOutputImpl
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.build.gradle.internal.dsl.DefaultConfig
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.compat.archivesBaseNameCompat
import net.twisterrob.gradle.kotlin.dsl.extensions
import net.twisterrob.gradle.kotlin.dsl.withId
import net.twisterrob.gradle.vcs.VCSExtension
import net.twisterrob.gradle.vcs.VCSPluginExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Locale
import java.util.Properties

/**
 * To use in Kotlin DSL use
 * ```kotlin
 * android.defaultConfig.version.…
 * ```
 * or
 * ```kotlin
 * android.defaultConfig.version {
 *     …
 * }
 * ```
 * Note: in Groovy DSL this is automatic.
 * @see version
 */
@Suppress("MemberVisibilityCanBePrivate")
open class AndroidVersionExtension {

	companion object {

		internal const val NAME: String = "version"
		internal const val DEFAULT_FILE_NAME: String = "version.properties"
	}

	private var autoVersionSet: Boolean = false

	/**
	 * Default versionCode pattern is MMMNNPPBBB (what fits into 2147483648).
	 * Adjust [minorMagnitude], [patchMagnitude] and [buildMagnitude] to change this.
	 *
	 * autoVersion will default to `true` when `version.properties` file exists.
	 * autoVersion will default to `true` when [major], [minor], [patch] or [build] properties are set.
	 */
	var autoVersion: Boolean = false
		set(value) {
			field = value
			autoVersionSet = true
		}

	var versionNameFormat: String = "%1\$d.%2\$d.%3\$d#%4\$d"

	var major: Int = 0 // M 0..213
		set(value) {
			field = value
			autoVersion()
		}

	var minor: Int = 0 // N 0..99
		set(value) {
			field = value
			autoVersion()
		}
	var minorMagnitude: Int = 100

	var patch: Int = 0 // P 0..99
		set(value) {
			field = value
			autoVersion()
		}
	var patchMagnitude: Int = 100

	var build: Int = 0 // B 0..999
		set(value) {
			field = value
			autoVersion()
		}

	private fun autoVersion() {
		if (!autoVersionSet) autoVersion = true
	}

	var buildMagnitude: Int = 1000

	/** VCS versionCode pattern is MMMNPBBBBB (what fits into 2147483648) */
	fun versionByVCS(vcs: VCSExtension) {
		// major magnitude is the rest // M 0..213
		minorMagnitude = 10 // N 0..9
		patchMagnitude = 10 // P 0..9
		buildMagnitude = 100000 // B 0..99999
		build = vcs.revisionNumber
	}

	var renameAPK: Boolean = true

	var formatArtifactName: (Project, String, String, Long, String?) -> String =
		{ project, baseName, applicationId, versionCode, versionName ->
			// strip project name, leave only variant
			val strippedBaseName = baseName.replace("${project.archivesBaseNameCompat}-", "")
			"${applicationId}@${versionCode}-v${versionName}+${strippedBaseName}"
		}
}

class AndroidVersionPlugin : BasePlugin() {

	private val android: AppExtension by lazy {
		if (!project.plugins.hasPlugin("com.android.application")) {
			throw PluginInstantiationException("Can only use versioning with Android applications")
		}
		project.extensions["android"] as AppExtension
	}

	private lateinit var version: AndroidVersionExtension

	override fun apply(target: Project) {
		super.apply(target)
		// When the Android application plugin is applied, we can set up the defaults and the DSL.
		project.plugins.withId<AppPlugin>("com.android.application") { init() }
		// Just before the project is finished evaluating, configure a bit more.
		when {
			AGPVersions.CLASSPATH >= AGPVersions.v70x -> {
				project.androidComponents.finalizeDsl { autoVersion() }
			}
			else -> {
				project.beforeAndroidTasksCreated { autoVersion() }
			}
		}
	}

	private fun init() {
		version = android.defaultConfig.extensions.create(AndroidVersionExtension.NAME)
		readVersionFromFile(project.file(AndroidVersionExtension.DEFAULT_FILE_NAME))

		when {
			AGPVersions.CLASSPATH >= AGPVersions.v70x -> {
				project.androidComponents.onVariants {
					if (version.renameAPK) {
						renameAPK7(project, version, it as ApplicationVariant)
					}
				}
			}
			else -> {
				project.beforeAndroidTasksCreated {
					android.applicationVariants.all {
						if (version.renameAPK) {
							renameAPK(it, it)
						}
					}
				}
			}
		}

		val vcs: VCSPluginExtension? = project.extensions.findByType()
		if (vcs != null && vcs.current.isAvailable) {
			version.versionByVCS(vcs.current)
		}
	}

	/**
	 * This method is called after the DSL has been parsed ([version] is ready),
	 * but before any of AGP's [Project.afterEvaluate] is executed.
	 */
	private fun autoVersion() {
		if (version.autoVersion) {
			android.defaultConfig.setVersionCode(calculateVersionCode())
			android.defaultConfig.setVersionName(calculateVersionName(null))
		}
	}

	/**
	 * AGP 4.1 doesn't propagate versionName and versionCode to androidTest variant any more.
	 *
	 * @param variant the APK to rename
	 * @param source the versioned APK
	 */
	private fun renameAPK(variant: ApkVariant, source: ApkVariant) {
		// only called for applicationVariants and their testVariants so filter should be safe
		variant.outputs.withType<ApkVariantOutput> {
			val artifactName = version.formatArtifactName(
				project, variant.baseName, variant.applicationId, source.versionCode.toLong(), source.versionName
			)
			outputFileName = "${artifactName}.apk"
		}
		if (variant is TestedVariant) {
			// TODO this is a Hail Mary trying to propagate version to androidTest APK, but has no effect.
			// Maybe? https://github.com/android/gradle-recipes/blob/bd8336e32ae512c630911287ea29b45a6bacb73b/BuildSrc/setVersionsFromTask/buildSrc/src/main/kotlin/CustomPlugin.kt
//			variant.testVariant?.outputs?.withType<ApkVariantOutput> {
//				versionNameOverride = source.versionName
//				versionCodeOverride = source.versionCode
//			}
			variant.testVariant?.run { renameAPK(this, source) }
		}
	}

	private fun calculateVersionName(variant: BaseVariant?): String {
		val suffix =
			if (variant != null)
				@Suppress("DEPRECATION")
				// It was changed from DefaultProductFlavor and deprecated in 4.0.0, keep it around until removal or relocation.
				com.android.builder.core.AbstractProductFlavor.mergeVersionNameSuffix(
					variant.buildType.versionNameSuffix,
					variant.mergedFlavor.versionNameSuffix
				)
			else
				""
		val versionName = version.versionNameFormat.format(
			Locale.ROOT,
			version.major, version.minor, version.patch, version.build
		)
		return versionName + suffix
	}

	private fun calculateVersionCode(): Int =
		(((version.major
				* version.minorMagnitude + version.minor)
				* version.patchMagnitude + version.patch)
				* version.buildMagnitude + version.build)

	private fun readVersionFromFile(file: File): Properties =
		readVersion(file).apply {
			getProperty("major")?.let { version.major = it.toInt() }
			getProperty("minor")?.let { version.minor = it.toInt() }
			getProperty("patch")?.let { version.patch = it.toInt() }
			getProperty("build")?.let { version.build = it.toInt() }
		}

	private fun readVersion(file: File): Properties =
		Properties().apply {
			try {
				FileInputStream(file)
					.use { load(it) } // Load the properties.
					.also { version.autoVersion = true } // If the file existed, turn on auto-versioning.
			} catch (ignore: FileNotFoundException) {
			}
		}
}

val DefaultConfig.version: AndroidVersionExtension
	get() = (this as ExtensionAware).extensions.getByName<AndroidVersionExtension>("version")

fun DefaultConfig.version(configuration: Action<AndroidVersionExtension>) {
	configuration.execute(version)
}

// STOPSHIP Note this has to be outside the class, otherwise Gradle reflection will fail on ComponentIdentity missing for AGP <7.
private fun renameAPK7(project: Project, version: AndroidVersionExtension, variant: ApplicationVariant) {
	// Only called for applicationVariants and their testVariants so filter should be safe.
	val variantOutput = variant.outputs.filterIsInstance<VariantOutputImpl>().single()
	val androidTestOutput = variant.androidTest?.let { androidTest ->
		androidTest as AndroidTestImpl
		androidTest.outputs.filterIsInstance<VariantOutputImpl>().single()
	}
	variantOutput.outputFileName.set(project.provider {
		// TODEL https://youtrack.jetbrains.com/issue/KTIJ-20208
		@Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
		val artifactName = version.formatArtifactName(
			project,
			variant.name,
			variant.applicationId.get(),
			variantOutput.versionCode.getOrElse(-1)!!.toLong(),
			variantOutput.versionName.getOrElse(null)
		)
		"${artifactName}.apk"
	})
	androidTestOutput?.let { androidTest ->
		androidTest.outputFileName.set(project.provider {
			// TODEL https://youtrack.jetbrains.com/issue/KTIJ-20208
			val androidTestName: String =
				variant.androidTest!!.name.removePrefix(variant.name).decapitalize(Locale.ROOT)
			@Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			val artifactName = version.formatArtifactName(
				project,
				"${variant.name}-${androidTestName}",
				variant.androidTest!!.applicationId.get(),
				variantOutput.versionCode.getOrElse(-1)!!.toLong(),
				variantOutput.versionName.getOrElse(null)
			)
			"${artifactName}.apk"
		})
	}
}
