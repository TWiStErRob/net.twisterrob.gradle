package net.twisterrob.gradle.android

import com.jakewharton.dex.DexMethod
import net.twisterrob.gradle.android.AndroidBuildPlugin.VERSION_BUILD_TOOLS
import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.Assert.assertThat
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal const val packageName = "net.twisterrob.gradle.test_app"
internal val packageFolder get() = packageName.replace('.', '/')

internal fun File.apk(
	variant: String,
	fileName: String = {
		val variantSuffix = if (variant != "release") ".${variant}" else ""
		val variantVersionSuffix = if (variant == "debug") "d" else ""
		"${packageName}${variantSuffix}@0-v0.0.0#0${variantVersionSuffix}+${variant}.apk"
	}()
) =
	this.resolve("build/outputs/apk").resolve(variant).resolve(fileName)

internal val GradleRunnerRule.root get () = this.getBuildFile().parentFile!!

internal fun BuildResult.assertNoTask(taskPath: String) = assertNull(task(taskPath))

/**
 * Assert that the task exists and that it ran to completion with success.
 * Note: this means that UP-TO-DATE and NO-SOURCE will fail!
 */
internal fun BuildResult.assertSuccess(taskPath: String) = assertOutcome(taskPath, TaskOutcome.SUCCESS)

internal fun BuildResult.assertFailed(taskPath: String) = assertOutcome(taskPath, TaskOutcome.FAILED)

internal fun BuildResult.assertOutcome(taskPath: String, outcome: TaskOutcome) {
	val task = task(taskPath)
		.let { assertNotNull(it, "${taskPath} task not found") }
	assertEquals(outcome, task.outcome)
}

internal fun String.normalize() = trim().replace("\r?\n".toRegex(), System.lineSeparator())
internal val buildToolsDir get () = File(System.getenv("ANDROID_HOME"), "build-tools/${VERSION_BUILD_TOOLS}")

internal fun resolveFromAndroidSDK(command: String) = resolveFromFolders(command, buildToolsDir)

internal fun resolveFromJDK(command: String): File {
	val jre = File(System.getProperty("java.home"))
	val dirs = arrayOf(
		jre.resolve("bin"),
		jre.parentFile.resolve("bin")
	)
	return resolveFromFolders(command, *dirs)
}

private fun resolveFromFolders(command: String, vararg dirs: File): File {
	val variants = listOf(command, "${command}.sh", "${command}.exe", "${command}.bat")
	return variants
		.flatMap { variant -> dirs.map { it.resolve(variant) } }
		.firstOrNull { it.exists() && it.isFile }
			?: error("Cannot find any of ${variants} in any of these folders:\n${dirs.joinToString("\n")}")
}

internal fun Iterable<String>.runCommand(
	workingDir: File = File("."),
	timeout: Long = TimeUnit.MINUTES.toMillis(60)
) =
	ProcessBuilder(this.toList())
		.directory(workingDir)
		.redirectOutput(ProcessBuilder.Redirect.PIPE)
		.redirectError(ProcessBuilder.Redirect.PIPE)
		.start()
		.apply { waitFor(timeout, TimeUnit.MILLISECONDS) }
		.run { inputStream.bufferedReader().readText() }

private fun assertOutput(command: List<Any>, expected: String) {
	val output = command.map(Any?::toString).runCommand()
	assertEquals(expected.normalize(), output.normalize())
}

internal fun assertDefaultDebugBadging(
	apk: File,
	applicationId: String = "${packageName}.debug",
	versionCode: String = "",
	versionName: String = "0.0.0#0d",
	compileSdkVersionName: String = AndroidBuildPlugin.VERSION_SDK_COMPILE_NAME,
	minSdkVersion: Int = AndroidBuildPlugin.VERSION_SDK_MINIMUM,
	targetSdkVersion: Int = AndroidBuildPlugin.VERSION_SDK_TARGET
) = assertDefaultBadging(
	apk,
	applicationId,
	versionCode,
	versionName,
	compileSdkVersionName,
	minSdkVersion,
	targetSdkVersion
)

internal fun assertDefaultReleaseBadging(
	apk: File,
	applicationId: String = packageName,
	versionCode: String = "",
	versionName: String = "0.0.0#0",
	compileSdkVersionName: String = AndroidBuildPlugin.VERSION_SDK_COMPILE_NAME,
	minSdkVersion: Int = AndroidBuildPlugin.VERSION_SDK_MINIMUM,
	targetSdkVersion: Int = AndroidBuildPlugin.VERSION_SDK_TARGET
) = assertDefaultBadging(
	apk,
	applicationId,
	versionCode,
	versionName,
	compileSdkVersionName,
	minSdkVersion,
	targetSdkVersion
)

internal fun assertDefaultBadging(
	apk: File,
	applicationId: String = "${packageName}.debug",
	versionCode: String = "",
	versionName: String = "0.0.0#0d",
	compileSdkVersionName: String = AndroidBuildPlugin.VERSION_SDK_COMPILE_NAME,
	minSdkVersion: Int = AndroidBuildPlugin.VERSION_SDK_MINIMUM,
	targetSdkVersion: Int = AndroidBuildPlugin.VERSION_SDK_TARGET
) {
	val fileNamesMessage =
		"Wanted: ${apk.absolutePath}${System.lineSeparator()}list: ${apk.parentFile.listFiles().joinToString(
			prefix = System.lineSeparator(),
			separator = System.lineSeparator()
		)}"
	assertThat(fileNamesMessage, apk, anExistingFile())
	assertOutput(
		listOf(resolveFromAndroidSDK("aapt"), "dump", "badging", apk),
		"""
					package: name='$applicationId' versionCode='$versionCode' versionName='$versionName' platformBuildVersionName='$compileSdkVersionName'
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
	)
}

fun dexMethod(className: String, methodName: String): Matcher<DexMethod> =
	object : TypeSafeMatcher<DexMethod>() {
		override fun describeTo(description: Description): Unit = with(description) {
			appendText("method ").appendValue(methodName)
			appendText(" ")
			appendText("in class ").appendValue(className)
		}

		override fun matchesSafely(item: DexMethod) =
			className == item.declaringType && methodName == item.name
	}
