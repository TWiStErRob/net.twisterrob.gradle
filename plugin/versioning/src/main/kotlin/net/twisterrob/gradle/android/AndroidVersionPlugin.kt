package net.twisterrob.gradle.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.build.gradle.internal.dsl.DefaultConfig
import net.twisterrob.gradle.base.BasePlugin
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
	}

	/** Default versionCode pattern is MMMNNPPBBB (what fits into 2147483648) */
	var autoVersion: Boolean = true
	var versionNameFormat: String = "%1\$d.%2\$d.%3\$d#%4\$d"
	var major: Int = 0 // M 0..213
	var minor: Int = 0 // N 0..99
	var minorMagnitude: Int = 100
	var patch: Int = 0 // P 0..99
	var patchMagnitude: Int = 100
	var build: Int = 0 // B 0..999
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

	private val version: AndroidVersionExtension by lazy {
		android.defaultConfig.extensions.create<AndroidVersionExtension>(AndroidVersionExtension.NAME)
	}

	override fun apply(target: Project) {
		super.apply(target)
		// When the Android application plugin is applied, we can set up the defaults and the DSL.
		project.plugins.withId<AppPlugin>("com.android.application") { init() }
		// Just before the project is finished evaluating, configure a bit more.
		project.beforeAndroidTasksCreated { configure() }
	}

	private fun init() {
		readVersionFromFile(project.file("version.properties"))

		val vcs: VCSPluginExtension? = project.extensions.findByType()
		if (vcs != null && vcs.current.isAvailable) {
			version.versionByVCS(vcs.current)
		}
	}

	/**
	 * This method is called after the DSL has been parsed ([version] is ready),
	 * but before any of AGP's [Project.afterEvaluate] is executed.
	 */
	private fun configure() {
		if (version.autoVersion) {
			android.defaultConfig.setVersionCode(calculateVersionCode())
			android.defaultConfig.setVersionName(calculateVersionName(null))
		}
		if (version.renameAPK) {
			android.applicationVariants.all { renameAPK(it, it) }
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
				FileInputStream(file).use { load(it) }
			} catch (ignore: FileNotFoundException) {
			}
		}
}

val DefaultConfig.version: AndroidVersionExtension
	get() = (this as ExtensionAware).extensions.getByName<AndroidVersionExtension>("version")

fun DefaultConfig.version(configuration: Action<AndroidVersionExtension>) {
	configuration.execute(version)
}
