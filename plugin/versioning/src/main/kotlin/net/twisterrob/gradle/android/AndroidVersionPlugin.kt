package net.twisterrob.gradle.android

import com.android.build.api.component.impl.AndroidTestImpl
import com.android.build.api.variant.AndroidTest
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.impl.VariantOutputImpl
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.DefaultConfig
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.internal.android.unwrapCast
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

	private var isAutoVersionSet: Boolean = false

	/**
	 * Default versionCode pattern is MMMNNPPBBB (what fits into 2147483648).
	 * Adjust [minorMagnitude], [patchMagnitude] and [buildMagnitude] to change this.
	 *
	 * autoVersion will default to `true` when `version.properties` file exists.
	 * autoVersion will default to `true` when [major], [minor], [patch] or [build] properties are set.
	 */
	var isAutoVersion: Boolean = false
		set(value) {
			field = value
			isAutoVersionSet = true
		}

	var versionNameFormat: (version: AndroidVersionExtension) -> String =
		{ version ->
			with(version) { "${major}.${minor}.${patch}#${build}" }
		}

	var versionCodeFormat: (version: AndroidVersionExtension) -> Int =
		{ version ->
			with(version) { ((major * minorMagnitude + minor) * patchMagnitude + patch) * buildMagnitude + build }
		}

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
	var minorMagnitude: Int = @Suppress("MagicNumber") 100

	var patch: Int = 0 // P 0..99
		set(value) {
			field = value
			autoVersion()
		}
	var patchMagnitude: Int = @Suppress("MagicNumber") 100

	var build: Int = 0 // B 0..999
		set(value) {
			field = value
			autoVersion()
		}

	var isRenameAPK: Boolean = true

	var formatArtifactName: (Project, String, String, Long, String?) -> String =
		{ _, variantName, applicationId, versionCode, versionName ->
			val variant =
				if (variantName.endsWith("AndroidTest")) {
					variantName.removeSuffix("AndroidTest") + "-androidTest"
				} else {
					variantName
				}
			"${applicationId}@${versionCode}-v${versionName ?: "null"}+${variant}"
		}

	var buildMagnitude: Int = @Suppress("MagicNumber") 1000

	/** VCS versionCode pattern is MMMNPBBBBB (which fits into 2147483648). */
	fun versionByVCS(vcs: VCSExtension) {
		// major magnitude is the rest // M 0..213
		minorMagnitude = @Suppress("MagicNumber") 10 // N 0..9
		patchMagnitude = @Suppress("MagicNumber") 10 // P 0..9
		buildMagnitude = @Suppress("MagicNumber") 100_000 // B 0..99999
		build = vcs.revisionNumber
		versionNameFormat = { version ->
			buildString {
				append(version.major)
				append(".")
				append(version.minor)
				append(".")
				append(version.patch)
				append("#")
				append(version.build)
				if (vcs.revision != version.build.toString()) {
					append("-")
					append(vcs.revision)
				}
			}
		}
	}

	fun versionByProperties(properties: Properties) {
		properties.getProperty("major")?.let { major = it.toInt() }
		properties.getProperty("minor")?.let { minor = it.toInt() }
		properties.getProperty("patch")?.let { patch = it.toInt() }
		properties.getProperty("build")?.let { build = it.toInt() }
	}

	private fun autoVersion() {
		if (!isAutoVersionSet) isAutoVersion = true
	}

	companion object {

		internal const val NAME: String = "version"
		internal const val DEFAULT_FILE_NAME: String = "version.properties"
	}
}

class AndroidVersionPlugin : BasePlugin() {

	private val android: AppExtension by lazy {
		if (!project.plugins.hasPlugin("com.android.application")) {
			throw PluginInstantiationException("Can only use versioning with Android applications")
		}
		project.extensions["android"] as AppExtension
	}

	@Suppress("LateinitUsage") // TODO can be probably refactored to put the when inside the withId and pass params.
	private lateinit var version: AndroidVersionExtension

	override fun apply(target: Project) {
		super.apply(target)
		// When the Android application plugin is applied, we can set up the defaults and the DSL.
		project.plugins.withId<AppPlugin>("com.android.application") { init() }
		// Just before the project is finished evaluating, configure a bit more.
		@Suppress("UseIfInsteadOfWhen") // Preparing for future new version ranges.
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
		version.versionByProperties(readVersion(project.file(AndroidVersionExtension.DEFAULT_FILE_NAME)))
		@Suppress("UseIfInsteadOfWhen") // Preparing for future new version ranges.
		when {
			AGPVersions.CLASSPATH >= AGPVersions.v70x -> {
				// AGP 7.4 compatibility: calling onVariants$default somehow changed, being explicit about params helps.
				project.androidComponents.onVariants(project.androidComponents.selector().all()) { variant ->
					if (version.isRenameAPK) {
						renameAPKPost7(variant as ApplicationVariant)
					}
				}
			}
			else -> {
				project.beforeAndroidTasksCreated {
					android.applicationVariants.all { variant ->
						if (version.isRenameAPK) {
							renameAPKPre7(variant, variant)
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
		if (version.isAutoVersion) {
			android.defaultConfig.setVersionCode(version.versionCodeFormat(version))
			android.defaultConfig.setVersionName(version.versionNameFormat(version))
		}
	}

	private fun renameAPKPost7(variant: ApplicationVariant) {
		val variantOutput = variant.outputs.filterIsInstance<VariantOutputImpl>().single()
		val androidTestOutput = variant.androidTestCompat?.let { androidTest ->
			val androidTestImpl = androidTest.unwrapCast<AndroidTest, AndroidTestImpl>()
			androidTestImpl.outputs.filterIsInstance<VariantOutputImpl>().single()
		}
		variantOutput.outputFileName.set(project.provider {
			// TODEL https://youtrack.jetbrains.com/issue/KTIJ-20208
			@Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UnsafeCallOnNullableType", "MaxLineLength")
			val artifactName = version.formatArtifactName(
				project,
				variant.name,
				variant.applicationId.get(),
				variantOutput.versionCode.getOrElse(-1)!!.toLong(),
				variantOutput.versionName.getOrElse(null)
			)
			artifactName.apk
		})
		androidTestOutput?.let { androidTest ->
			androidTest.outputFileName.set(project.provider {
				// TODEL https://youtrack.jetbrains.com/issue/KTIJ-20208
				@Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UnsafeCallOnNullableType", "MaxLineLength")
				val artifactName = version.formatArtifactName(
					project,
					variant.androidTestCompat!!.name,
					variant.androidTestCompat!!.applicationId.get(),
					variantOutput.versionCode.getOrElse(-1)!!.toLong(),
					variantOutput.versionName.getOrElse(null)
				)
				artifactName.apk
			})
		}
	}

	/**
	 * AGP 4.1 doesn't propagate versionName and versionCode to androidTest variant any more.
	 *
	 * @param variant the APK to rename
	 * @param source the versioned APK
	 */
	private fun renameAPKPre7(
		variant: @Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.api.ApkVariant,
		source: @Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.api.ApkVariant
	) {
		// only called for applicationVariants and their testVariants so filter should be safe
		variant.outputs.withType<@Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.api.ApkVariantOutput> {
			val artifactName = version.formatArtifactName(
				project, variant.name, variant.applicationId, source.versionCode.toLong(), source.versionName
			)
			outputFileName = artifactName.apk
		}
		if (variant is @Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.internal.api.TestedVariant) {
			// TODO this is a Hail Mary trying to propagate version to androidTest APK, but has no effect.
			// Maybe? https://github.com/android/gradle-recipes/blob/bd8336e32ae512c630911287ea29b45a6bacb73b/BuildSrc/setVersionsFromTask/buildSrc/src/main/kotlin/CustomPlugin.kt
//			variant.testVariant?.outputs?.withType<ApkVariantOutput> {
//				versionNameOverride = source.versionName
//				versionCodeOverride = source.versionCode
//			}
			variant.testVariant?.run { renameAPKPre7(this, source) }
		}
	}

	private fun readVersion(file: File): Properties =
		Properties().also { props ->
			try {
				FileInputStream(file).use { props.load(it) }
				// If the file existed, turn on auto-versioning. TODO remove side effect.
				version.isAutoVersion = true
			} catch (ignore: FileNotFoundException) {
			}
		}
}

val DefaultConfig.version: AndroidVersionExtension
	get() = (this as ExtensionAware).extensions.getByName<AndroidVersionExtension>("version")

fun DefaultConfig.version(configuration: Action<AndroidVersionExtension>) {
	configuration.execute(version)
}

private val String.apk: String
	get() = "${this}.apk"
