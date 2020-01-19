package net.twisterrob.gradle.android

import com.android.ddmlib.AndroidDebugBridge
import com.jakewharton.dex.DexMethod
import net.twisterrob.test.process.assertOutput
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.io.FileMatchers.anExistingFile
import java.io.File
import kotlin.test.assertTrue

internal const val packageName = "net.twisterrob.gradle.test_app"
internal val packageFolder get() = packageName.replace('.', '/')

internal fun File.apk(
	variant: String,
	fileName: String = {
		val variantSuffix = if (variant != "release") ".${variant}" else ""
		val variantVersionSuffix = if (variant == "debug") "d" else ""
		"${packageName}${variantSuffix}@1-v0.0.0#1${variantVersionSuffix}+${variant}.apk"
	}()
): File =
	this.resolve("build/outputs/apk").resolve(variant).resolve(fileName)

internal val androidSdkDir: File
	get() = File(System.getenv("ANDROID_HOME"))

internal val buildToolsDir: File
	get() = androidSdkDir.resolve("build-tools/${VERSION_BUILD_TOOLS}")

internal val toolsDir: File
	get() = androidSdkDir.resolve("tools")

internal val toolsBinDir: File
	get() = androidSdkDir.resolve("tools/bin")

internal val platformToolsDir: File
	get() = androidSdkDir.resolve("platform-tools")

internal fun resolveFromAndroidSDK(command: String): File =
	resolveFromFolders(command, buildToolsDir, platformToolsDir, toolsDir, toolsBinDir)

internal fun resolveFromJDK(command: String): File {
	val jre = File(System.getProperty("java.home"))
	val dirs = arrayOf(
		jre.resolve("bin"),
		jre.parentFile.resolve("bin")
	)
	return resolveFromFolders(command, *dirs)
}

private fun resolveFromFolders(command: String, vararg dirs: File): File {
	val variants = listOf("${command}.sh", command, "${command}.bat", "${command}.exe")
	return variants
		.flatMap { variant -> dirs.map { it.resolve(variant) } }
		.firstOrNull { it.exists() && it.isFile }
		?: error("Cannot find any of ${variants} in any of these folders:\n${dirs.joinToString("\n")}")
}

internal fun assertDefaultDebugBadging(
	apk: File,
	applicationId: String = "${packageName}.debug",
	versionCode: String = "1",
	versionName: String = "0.0.0#1d",
	compileSdkVersion: Int = VERSION_SDK_COMPILE,
	compileSdkVersionName: String = VERSION_SDK_COMPILE_NAME,
	minSdkVersion: Int = VERSION_SDK_MINIMUM,
	targetSdkVersion: Int = VERSION_SDK_TARGET
) = assertDefaultBadging(
	apk,
	applicationId,
	versionCode,
	versionName,
	compileSdkVersion,
	compileSdkVersionName,
	minSdkVersion,
	targetSdkVersion
)

internal fun assertDefaultReleaseBadging(
	apk: File,
	applicationId: String = packageName,
	versionCode: String = "1",
	versionName: String = "0.0.0#1",
	compileSdkVersion: Int = VERSION_SDK_COMPILE,
	compileSdkVersionName: String = VERSION_SDK_COMPILE_NAME,
	minSdkVersion: Int = VERSION_SDK_MINIMUM,
	targetSdkVersion: Int = VERSION_SDK_TARGET
) = assertDefaultBadging(
	apk,
	applicationId,
	versionCode,
	versionName,
	compileSdkVersion,
	compileSdkVersionName,
	minSdkVersion,
	targetSdkVersion
)

internal fun assertDefaultBadging(
	apk: File,
	applicationId: String = "${packageName}.debug",
	versionCode: String = "1",
	versionName: String = "0.0.0#1d",
	compileSdkVersion: Int = VERSION_SDK_COMPILE,
	compileSdkVersionName: String = VERSION_SDK_COMPILE_NAME,
	minSdkVersion: Int = VERSION_SDK_MINIMUM,
	targetSdkVersion: Int = VERSION_SDK_TARGET
) {
	val fileNamesMessage =
		"Wanted: ${apk.absolutePath}${System.lineSeparator()}list: ${apk.parentFile.listFiles().joinToString(
			prefix = System.lineSeparator(),
			separator = System.lineSeparator()
		)}"
	assertThat(fileNamesMessage, apk, anExistingFile())
	val expectedOutput =
		if (compileSdkVersion < 28) {
			// platformBuildVersionName='$compileSdkVersionName' disappeared in AGP 3.3 and/or AAPT 2
			"""
				package: name='$applicationId' versionCode='$versionCode' versionName='$versionName'
				sdkVersion:'$minSdkVersion'
				targetSdkVersion:'$targetSdkVersion'
				feature-group: label=''
				  uses-feature: name='android.hardware.faketouch'
				  uses-implied-feature: name='android.hardware.faketouch' reason='default feature for all apps'
				supports-screens: 'small' 'normal' 'large' 'xlarge'
				supports-any-density: 'true'
				locales: '--_--'
				densities: '160'
			""".trimIndent()
		} else {
			"""
				package: name='$applicationId' versionCode='$versionCode' versionName='$versionName' compileSdkVersion='$compileSdkVersion' compileSdkVersionCodename='$compileSdkVersionName'
				sdkVersion:'$minSdkVersion'
				targetSdkVersion:'$targetSdkVersion'
				feature-group: label=''
				  uses-feature: name='android.hardware.faketouch'
				  uses-implied-feature: name='android.hardware.faketouch' reason='default feature for all apps'
				supports-screens: 'small' 'normal' 'large' 'xlarge'
				supports-any-density: 'true'
				locales: '--_--'
				densities: '160'
			""".trimIndent()
		}
	assertOutput(listOf(resolveFromAndroidSDK("aapt"), "dump", "badging", apk), expectedOutput)
}

fun dexMethod(className: String, methodName: String): Matcher<DexMethod> =
	object : TypeSafeMatcher<DexMethod>() {
		override fun describeTo(description: Description): Unit = with(description) {
			appendText("method ").appendValue(methodName)
			appendText(" ")
			appendText("in class ").appendValue(className)
		}

		override fun matchesSafely(item: DexMethod) =
			className == item.declaringType.sourceName && methodName == item.name
	}

fun hasDevices(): Boolean {
	AndroidDebugBridge.initIfNeeded(false)
	val bridge = AndroidDebugBridge.createBridge(resolveFromAndroidSDK("adb").absolutePath, false)

	var wait = 5000L
	while (!bridge.hasInitialDeviceList() && wait > 0) {
		Thread.sleep(wait)
		wait -= 1000L
	}
	assertTrue(wait > 0 && bridge.hasInitialDeviceList(), "Cannot get device list")
	return bridge.devices.isNotEmpty()
}
