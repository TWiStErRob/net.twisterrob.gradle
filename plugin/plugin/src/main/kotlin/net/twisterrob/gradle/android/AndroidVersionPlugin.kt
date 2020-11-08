package net.twisterrob.gradle.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.AndroidBasePlugin
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import net.twisterrob.gradle.base.BasePlugin
import net.twisterrob.gradle.kotlin.dsl.base
import net.twisterrob.gradle.kotlin.dsl.extensions
import net.twisterrob.gradle.kotlin.dsl.withId
import net.twisterrob.gradle.vcs.VCSExtension
import net.twisterrob.gradle.vcs.VCSPluginExtension
import org.gradle.api.Project
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Locale

@Suppress("MemberVisibilityCanBePrivate")
open class AndroidVersionExtension {

	companion object {

		internal const val NAME = "version"
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

	/**
	 * Bridge for Groovy callers.
	 * @see formatArtifactName property
	 */
	fun formatArtifactName(project: Project, variant: ApkVariant, baseName: String) =
		(formatArtifactName)(project, variant, baseName)

	var formatArtifactName: (Project, ApkVariant, String) -> String = { project, variant, baseName ->
		// strip project name, leave only variant
		val strippedBaseName = baseName.replace("${project.base.archivesBaseName}-", "")
		"${variant.applicationId}@${variant.versionCode}-v${variant.versionName}+${strippedBaseName}"
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

	/**
	 * There's a tricky execution order required for this plugin to work:
	 *  * It has to be applied before any Android plugin.
	 *  * It has to hook into the right lifecycle to make sure data is propagated where it's needed.
	 *  * Default [version] extension info has to be initialized before [android] DSL is accessed from `build.gradle`.
	 *  * [version] extension has to be ready by the time it is used in [configure].
	 *
	 * 1. The [DefaultProductFlavor.getVersionName] is used by the Android plugin to propagate the version information.
	 * 2. This happens deep inside [com.android.build.gradle.BasePlugin.createAndroidTasks] (since 3.?, last check 3.2-beta05):
	 *  * [com.android.build.gradle.internal.VariantManager.createAndroidTasks]
	 *  * [com.android.build.gradle.internal.VariantManager.populateVariantDataList]
	 *  * [com.android.build.gradle.internal.VariantManager.createVariantDataForProductFlavors]
	 *  * [com.android.build.gradle.internal.VariantManager.createVariantDataForProductFlavorsAndVariantType]
	 *  * [com.android.build.gradle.internal.VariantManager.createVariantDataForVariantType]
	 *  * [com.android.build.gradle.internal.core.GradleVariantConfiguration.VariantConfigurationBuilder.create]
	 *  * [com.android.build.gradle.internal.core.GradleVariantConfiguration] constructor
	 *  * [com.android.build.gradle.internal.core.VariantConfiguration] constructor
	 *  * that merges in DefaultConfig
	 *  * in [com.android.builder.core.DefaultProductFlavor._initWith]: `this.mVersionName = thatProductFlavor.versionName`
	 * 3. So the `versionName` has to be set before the tasks are created in `afterEvaluate`.
	 */
	override fun apply(target: Project) {
		super.apply(target)
		// Order of execution denoted with /*[C#]*/ for Configuration Phase, and /*[A#]*/ for afterEvaluate
		// This method body is /*[C0]*/
		if (project.plugins.hasPlugin("com.android.base")) {
			throw PluginInstantiationException("This plugin must be applied before the android plugins")
		}
		// just to make sure we're in the right module (see lazy initializer of android)
		project.afterEvaluate { android }
		// when we detect that an Android plugin is going to be applied
		project.plugins.withType<AndroidBasePlugin> {
			// enqueue afterEvaluate, so it runs before BasePlugin.createAndroidTasks /*[A5]*/
			// see BasePlugin.createTasks /*[C2]*/ as to how createAndroidTasks is called
			project.afterEvaluate { configure() /*[A4]*/ } /*[C1]*/
		}
		// when the Android application plugin is applied, we can set up the defaults and the DSL
		project.plugins.withId<AppPlugin>("com.android.application") {
			init() /*[C3]*/
		}
	}

	private fun init() {
		readVersionFromFile(project.file("version.properties"))

		val vcs = project.extensions.findByName(VCSPluginExtension.NAME) as VCSPluginExtension?
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
			android.defaultConfig.versionCode = calculateVersionCode()
			android.defaultConfig.versionName = calculateVersionName(null)
		}
		if (version.renameAPK) {
			android.applicationVariants.all(::renameAPK)
		}
	}

	private fun renameAPK(variant: ApkVariant) {
		// only called for applicationVariants and their testVariants so filter should be safe
		variant.outputs.filterIsInstance<ApkVariantOutput>().forEach { output ->
			output.outputFileName = calculateOutputFileName(variant)
		}
		if (variant is TestedVariant) {
			variant.testVariant?.run(::renameAPK)
		}
	}

	private fun calculateOutputFileName(variant: ApkVariant): String {
		val artifactName = version.formatArtifactName(project, variant, variant.baseName)
		return "${artifactName}.apk"
	}

	@Suppress("DEPRECATION")
	@Deprecated("new method is using defaultConfig")
	private fun autoVersion(variant: ApkVariant) {
		variant.outputs.filterIsInstance<ApkVariantOutput>().forEach { output ->
			output.versionNameOverride = calculateVersionName(variant)
			output.versionCodeOverride = calculateVersionCode()
		}
		// need to version the test variant, so the androidTest APK gets the same version its AndroidManifest
		if (variant is TestedVariant) {
			variant.testVariant?.run(::autoVersion)
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

	private fun readVersionFromFile(file: File) =
		readVersion(file).apply {
			getProperty("major")?.let { version.major = it.toInt() }
			getProperty("minor")?.let { version.minor = it.toInt() }
			getProperty("patch")?.let { version.patch = it.toInt() }
			getProperty("build")?.let { version.build = it.toInt() }
		}

	private fun readVersion(file: File) = java.util.Properties().apply {
		try {
			FileInputStream(file).use { load(it) }
		} catch (ignore: FileNotFoundException) {
		}
	}
}
