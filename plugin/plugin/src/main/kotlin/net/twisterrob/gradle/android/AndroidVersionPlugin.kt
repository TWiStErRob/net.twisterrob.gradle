package net.twisterrob.gradle.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.builder.core.DefaultProductFlavor
import net.twisterrob.gradle.common.BasePluginForKotlin
import net.twisterrob.gradle.kotlin.dsl.base
import net.twisterrob.gradle.kotlin.dsl.extensions
import net.twisterrob.gradle.vcs.VCSExtension
import net.twisterrob.gradle.vcs.VCSPluginExtension
import org.gradle.api.Project
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.kotlin.dsl.get
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
open class AndroidVersionExtension {

	/** Default versionCode pattern is MMMNNPPBBB (what fits into 2147483648) */
	var autoVersion: Boolean = true
	var versionNameFormat: String = "%1\$d.%2\$d.%3\$d#%4\$d"
	var versionFile: File? = File("version.properties")
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

class AndroidVersionPlugin : BasePluginForKotlin() {

	private lateinit var version: AndroidVersionExtension

	override fun apply(target: Project) {
		super.apply(target)

		if (!project.plugins.hasPlugin("com.android.application")) {
			throw PluginInstantiationException("Can only use versioning with Android applications")
		}
		val android = project.extensions["android"] as AppExtension
		version = android.defaultConfig.extensions.create("version", AndroidVersionExtension::class.java)
		// resolve default version file against project
		version.versionFile = project.file(version.versionFile!!.name)

		val vcs = project.extensions.findByName("VCS") as VCSPluginExtension?
		if (vcs != null && vcs.current.isAvailable) {
			version.versionByVCS(vcs.current)
		}

		// set up defaults to use, before afterEvaluate so the user has a chance to override
		version.versionFile?.let { readVersionFromFile(it) }
		if (version.autoVersion) {
			android.defaultConfig.apply {
				versionName = calculateVersionName()
				versionCode = calculateVersionCode()
			}
		}
		// later, read the user's setup (if any) and act accordingly
		project.afterEvaluate {
			android.applicationVariants.all { variant ->
				version.versionFile?.let { readVersionFromFile(it) }
				if (version.autoVersion) {
					autoVersion(variant)
				}
				if (version.renameAPK) {
					appendVersionNameVersionCode(variant)
				}
			}
		}
	}

	private fun appendVersionNameVersionCode(variant: ApkVariant) {
		for (output in variant.outputs) {
			// only called for applicationVariants and their testVariants so cast should be safe
			updateOutput(output as ApkVariantOutput, variant)
		}
		if (variant is TestedVariant && variant.testVariant != null) {
			appendVersionNameVersionCode(variant.testVariant)
		}
	}

	private fun updateOutput(output: ApkVariantOutput, variant: ApkVariant) {
		val artifactName = version.formatArtifactName(project, variant, output.baseName)
		output.outputFileName = "${artifactName}.apk"
	}

	/**
	 * Late-initialize version related fields in the variant,
	 * all places where it was cached must be updated, because we delayed ourselves into afterEvaluate.
	 */
	private fun autoVersion(variant: BaseVariant) {
		(variant.mergedFlavor as DefaultProductFlavor).versionName = calculateVersionName()
		(variant.mergedFlavor as DefaultProductFlavor).versionCode = calculateVersionCode()
		for (output in variant.outputs) {
			if (output is ApkVariantOutput && variant is ApkVariant) {
				// update the APK's AndroidManifest.xml data to match the outside world
				output.versionCodeOverride = variant.versionCode
				output.versionNameOverride = variant.versionName
			}
		}
		if (variant is TestedVariant && variant.testVariant != null) {
			// need to version the test variant, so the androidTest APK gets the same version its AndroidManifest
			autoVersion(variant.testVariant)
		}
	}

	private fun calculateVersionName() = String.format(
		Locale.ROOT,
		version.versionNameFormat,
		version.major, version.minor, version.patch, version.build
	)

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
