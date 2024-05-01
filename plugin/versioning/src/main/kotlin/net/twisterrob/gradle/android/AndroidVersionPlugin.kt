package net.twisterrob.gradle.android

import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.ComponentIdentity
import com.android.build.api.variant.GeneratesApk
import com.android.build.api.variant.impl.VariantOutputImpl
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.DefaultConfig
import net.twisterrob.gradle.common.BasePlugin
import net.twisterrob.gradle.ext.zip
import net.twisterrob.gradle.internal.android.unwrapCast
import net.twisterrob.gradle.kotlin.dsl.extensions
import net.twisterrob.gradle.kotlin.dsl.withId
import net.twisterrob.gradle.vcs.VCSExtension
import net.twisterrob.gradle.vcs.VCSPluginExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
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
@Suppress(
	"MemberVisibilityCanBePrivate",
	"detekt.UnnecessaryAbstractClass", // Gradle convention.
)
abstract class AndroidVersionExtension {

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
	var minorMagnitude: Int = @Suppress("detekt.MagicNumber") 100

	var patch: Int = 0 // P 0..99
		set(value) {
			field = value
			autoVersion()
		}
	var patchMagnitude: Int = @Suppress("detekt.MagicNumber") 100

	var build: Int = 0 // B 0..999
		set(value) {
			field = value
			autoVersion()
		}

	var buildMagnitude: Int = @Suppress("detekt.MagicNumber") 1000

	var versionNameFormat: (version: AndroidVersionExtension) -> String =
		{ version ->
			with(version) { "${major}.${minor}.${patch}#${build}" }
		}

	var versionCodeFormat: (version: AndroidVersionExtension) -> Int =
		{ version ->
			with(version) { ((major * minorMagnitude + minor) * patchMagnitude + patch) * buildMagnitude + build }
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

	/** VCS versionCode pattern is MMMNPBBBBB (which fits into 2147483648). */
	fun versionByVCS(vcs: VCSExtension) {
		// major magnitude is the rest // M 0..213
		minorMagnitude = @Suppress("detekt.MagicNumber") 10 // N 0..9
		patchMagnitude = @Suppress("detekt.MagicNumber") 10 // P 0..9
		buildMagnitude = @Suppress("detekt.MagicNumber") 100_000 // B 0..99999
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

		fun from(android: BaseExtension): AndroidVersionExtension =
			from(android.defaultConfig)

		fun from(defaultConfig: DefaultConfig): AndroidVersionExtension =
			defaultConfig.extensions.getByName<AndroidVersionExtension>(NAME)
	}
}

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class AndroidVersionPlugin : BasePlugin() {

	private val android: AppExtension by lazy {
		if (!project.plugins.hasPlugin("com.android.application")) {
			throw PluginInstantiationException("Can only use versioning with Android applications")
		}
		project.extensions["android"] as AppExtension
	}

	@Suppress("detekt.LateinitUsage") // TODO can be probably refactored to put the when inside the withId and pass params.
	private lateinit var version: AndroidVersionExtension

	override fun apply(target: Project) {
		super.apply(target)
		// When the Android application plugin is applied, we can set up the defaults and the DSL.
		project.plugins.withId<AppPlugin>("com.android.application") { init() }
		// Just before the project is finished evaluating, configure a bit more.
		project.androidComponents.finalizeDsl { autoVersion() }
	}

	private fun init() {
		version = android.defaultConfig.extensions.create(AndroidVersionExtension.NAME)
		version.versionByProperties(readVersion(project.file(AndroidVersionExtension.DEFAULT_FILE_NAME)))
		project.androidComponents.onVariantsCompat { variant ->
			if (version.isRenameAPK) {
				renameAPK(variant as ApplicationVariant)
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

	private fun renameAPK(variant: ApplicationVariant) {
		// TODO replace with new Variant API transformation. https://github.com/TWiStErRob/net.twisterrob.gradle/issues/456
		val variantOutput = variant.outputs.filterIsInstance<VariantOutputImpl>().single()
		@Suppress("UNCHECKED_CAST")
		val versionCode = variantOutput.versionCode.orElse(-1) as Provider<Int>
		@Suppress("UNCHECKED_CAST")
		val versionName = variantOutput.versionName.orElse("null") as Provider<String>

		variantOutput.outputFileName.set(
			variant.replacementApkNameProvider(versionCode, versionName)
		)

		variant.androidTestCompat?.let { androidTest ->
			androidTest.unwrapCast().setOutputFileName(
				apkName = androidTest.replacementApkNameProvider(versionCode, versionName),
				project = project,
				variant = variant.name
			)
		}
	}

	private fun <T> T.replacementApkNameProvider(
		versionCodeProvider: Provider<Int>,
		versionNameProvider: Provider<String>
	): Provider<String> where T : ComponentIdentity, T : GeneratesApk =
		this.applicationId.zip(versionCodeProvider, versionNameProvider) { applicationId, versionCode, versionName ->
			val artifactName = version.formatArtifactName(
				project,
				this.name,
				applicationId,
				versionCode.toLong(),
				versionName
			)
			artifactName.apk
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
	get() = AndroidVersionExtension.from(this)

fun DefaultConfig.version(configuration: Action<AndroidVersionExtension>) {
	configuration.execute(version)
}

private val String.apk: String
	get() = "${this}.apk"
