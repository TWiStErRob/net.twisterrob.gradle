package net.twisterrob.gradle.android

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.jakewharton.dex.DexMethod
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.test.process.assertOutput
import net.twisterrob.test.withRootCause
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @see /android-plugin_app/build.gradle
 */
@Suppress("TopLevelPropertyNaming") // Match style of AGP.
const val packageName: String = "net.twisterrob.gradle.test_app"

val packageFolder: String
	get() = packageName.replace('.', '/')

fun File.apk(
	variant: String,
	fileName: String = run {
		val variantSuffix = if (variant != "release") ".${variant}" else ""
		"${packageName}${variantSuffix}@-1-v+${variant}.apk"
	}
): File =
	this.resolve("build/outputs/apk").resolve(variant).resolve(fileName)

internal val androidSdkDir: File
	get() = File(System.getenv("ANDROID_HOME"))

internal val buildToolsDir: File
	get() = androidSdkDir.resolve("build-tools").listFiles().sorted().last()

internal val toolsDir: File
	get() = androidSdkDir.resolve("tools")

internal val toolsBinDir: File
	get() = androidSdkDir.resolve("tools/bin")

internal val platformToolsDir: File
	get() = androidSdkDir.resolve("platform-tools")

fun resolveFromAndroidSDK(command: String): File =
	resolveFromFolders(command, buildToolsDir, platformToolsDir, toolsDir, toolsBinDir)

fun resolveFromJDK(command: String): File {
	val jre = File(System.getProperty("java.home"))
	val dirs = arrayOf(
		jre.resolve("bin"),
		jre.parentFile.resolve("bin")
	)
	@Suppress("detekt.SpreadOperator")
	return resolveFromFolders(command, *dirs)
}

private fun resolveFromFolders(command: String, vararg dirs: File): File {
	val variants = listOf("${command}.sh", command, "${command}.bat", "${command}.exe")
	return variants
		.flatMap { variant -> dirs.map { it.resolve(variant) } }
		.firstOrNull { it.exists() && it.isFile }
		?: error("Cannot find any of ${variants} in any of these folders:\n${dirs.joinToString("\n")}")
}

@Suppress("detekt.LongParameterList") // Simply these many things contribute to APK metadata.
fun assertDefaultDebugBadging(
	apk: File,
	applicationId: String = "${packageName}.debug",
	versionCode: String = "",
	versionName: String = "",
	compileSdkVersion: Int = System.getProperty("net.twisterrob.test.android.compileSdkVersion")
		.removePrefix("android-")
		.toInt(),
	compileSdkVersionName: String = (compileSdkVersion - 20).toString(),
	minSdkVersion: Int = VERSION_SDK_MINIMUM,
	targetSdkVersion: Int = VERSION_SDK_TARGET
) {
	assertDefaultBadging(
		apk = apk,
		applicationId = applicationId,
		versionCode = versionCode,
		versionName = versionName,
		compileSdkVersion = compileSdkVersion,
		compileSdkVersionName = compileSdkVersionName,
		minSdkVersion = minSdkVersion,
		targetSdkVersion = targetSdkVersion,
		isDebuggable = true,
	)
}

@Suppress("detekt.LongParameterList") // Simply these many things contribute to APK metadata.
fun assertDefaultReleaseBadging(
	apk: File,
	applicationId: String = packageName,
	versionCode: String = "",
	versionName: String = "",
	compileSdkVersion: Int = System.getProperty("net.twisterrob.test.android.compileSdkVersion")
		.removePrefix("android-")
		.toInt(),
	compileSdkVersionName: String = (compileSdkVersion - 20).toString(),
	minSdkVersion: Int = VERSION_SDK_MINIMUM,
	targetSdkVersion: Int = VERSION_SDK_TARGET
) {
	assertDefaultBadging(
		apk = apk,
		applicationId = applicationId,
		versionCode = versionCode,
		versionName = versionName,
		compileSdkVersion = compileSdkVersion,
		compileSdkVersionName = compileSdkVersionName,
		minSdkVersion = minSdkVersion,
		targetSdkVersion = targetSdkVersion,
		isDebuggable = false,
	)
}

@Suppress("detekt.LongParameterList", "detekt.LongMethod") // Simply these many things contribute to APK metadata.
fun assertDefaultBadging(
	apk: File,
	applicationId: String = "${packageName}.debug",
	versionCode: String = "",
	versionName: String = "",
	compileSdkVersion: Int = System.getProperty("net.twisterrob.test.android.compileSdkVersion")
		.removePrefix("android-")
		.toInt(),
	compileSdkVersionName: String = (compileSdkVersion - 20).toString(),
	minSdkVersion: Int = VERSION_SDK_MINIMUM,
	targetSdkVersion: Int = VERSION_SDK_TARGET,
	isAndroidTestApk: Boolean = false,
	isDebuggable: Boolean = true,
) {
	try {
		assertThat(apk.absolutePath, apk, anExistingFile())
	} catch (@Suppress("detekt.SwallowedException", "detekt.TooGenericExceptionCaught") ex: Throwable) {
		// Detekt doesn't see into the extension fun.
		val contents = apk
			.parentFile
			.listFiles()
			.orEmpty()
			.joinToString(prefix = "'${apk.parentFile}' contents:\n", separator = "\n")
		throw ex.withRootCause(IOException(contents))
	}
	val applicationDebuggable =
		if (AGPVersions.v81x <= AGPVersions.UNDER_TEST && isDebuggable) {
			"application-debuggable"
		} else {
			null
		}
	val (expectation: String, expectedOutput: String) =
		if (compileSdkVersion < @Suppress("detekt.MagicNumber") 28) {
			// platformBuildVersionName='$compileSdkVersionName' disappeared in AGP 3.3 and/or AAPT 2
			"compileSdkVersion < 28" to """
				package: name='$applicationId' versionCode='$versionCode' versionName='$versionName'
				sdkVersion:'$minSdkVersion'
				targetSdkVersion:'$targetSdkVersion'
				application: label='' icon=''${applicationDebuggable?.prependIndent("\n\t\t\t\t").orEmpty()}
				feature-group: label=''
				  uses-feature: name='android.hardware.faketouch'
				  uses-implied-feature: name='android.hardware.faketouch' reason='default feature for all apps'
				supports-screens: 'small' 'normal' 'large' 'xlarge'
				supports-any-density: 'true'
				locales: '--_--'
				densities: '160'
			""".trimIndent()
		} else {
			if (!isAndroidTestApk) {
				"compileSdkVersion >= 28 && !isAndroidTestApk" to """
					package: name='$applicationId' versionCode='$versionCode' versionName='$versionName' platformBuildVersionName='$compileSdkVersionName' platformBuildVersionCode='$compileSdkVersion' compileSdkVersion='$compileSdkVersion' compileSdkVersionCodename='$compileSdkVersionName'
					sdkVersion:'$minSdkVersion'
					targetSdkVersion:'$targetSdkVersion'
					application: label='' icon=''${applicationDebuggable?.prependIndent("\n\t\t\t\t\t").orEmpty()}
					feature-group: label=''
					  uses-feature: name='android.hardware.faketouch'
					  uses-implied-feature: name='android.hardware.faketouch' reason='default feature for all apps'
					supports-screens: 'small' 'normal' 'large' 'xlarge'
					supports-any-density: 'true'
					locales: '--_--'
					densities: '160'
				""".trimIndent()
			} else {
				// TODO versionCode and versionName is not verified!
				"compileSdkVersion >= 28 && isAndroidTestApk" to """
					package: name='$applicationId' versionCode='' versionName='' platformBuildVersionName='$compileSdkVersionName' platformBuildVersionCode='$compileSdkVersion' compileSdkVersion='$compileSdkVersion' compileSdkVersionCodename='$compileSdkVersionName'
					sdkVersion:'$minSdkVersion'
					targetSdkVersion:'$targetSdkVersion'
					application: label='' icon=''
					application-debuggable
					uses-library:'android.test.runner'
					feature-group: label=''
					  uses-feature: name='android.hardware.faketouch'
					  uses-implied-feature: name='android.hardware.faketouch' reason='default feature for all apps'
					supports-screens: 'small' 'normal' 'large' 'xlarge'
					supports-any-density: 'true'
					locales: '--_--'
					densities: '160'
				""".trimIndent()
			}
		}
	assertOutput(listOf(resolveFromAndroidSDK("aapt"), "dump", "badging", apk), expectedOutput, expectation)
}

fun dexMethod(className: String, methodName: String): Matcher<DexMethod> =
	object : TypeSafeMatcher<DexMethod>() {
		override fun describeTo(description: Description) {
			with(description) {
				appendText("method ").appendValue(methodName)
				appendText(" ")
				appendText("in class ").appendValue(className)
			}
		}

		override fun matchesSafely(item: DexMethod): Boolean =
			className == GradleTestHelpers.sourceName(item) && methodName == item.name
	}

fun devices(): List<IDevice> {
	@Suppress("DEPRECATION") // REPORT Don't know why, cannot fix it.
	AndroidDebugBridge.initIfNeeded(false)
	val bridge = AndroidDebugBridge.createBridge(
		resolveFromAndroidSDK("adb").absolutePath,
		false,
		@Suppress("detekt.MagicNumber") 10,
		TimeUnit.SECONDS
	)
	@Suppress("detekt.MagicNumber")
	ensuredWait(5000L, 1000L, "Cannot get device list") {
		bridge.hasInitialDeviceList()
	}
	return bridge.devices.toList()
}

fun ensuredWait(initialWait: Long, reduceAmount: Long, message: String, block: () -> Boolean) {
	var wait = initialWait
	while (!block() && wait > 0) {
		Thread.sleep(wait)
		wait -= reduceAmount
	}
	assertTrue(wait > 0 && block(), message)
}
