package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.root
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * @see AndroidBuildPlugin
 */
class AndroidBuildPluginIntgTest : BaseAndroidIntgTest() {

	@Test fun `default build setup is simple and produces default output (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug")
		)
	}

	@Test fun `default build setup is simple and produces default output (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release")
		)
	}

	@Test fun `can override minSdkVersion (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.minSdkVersion = 10
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug"),
			minSdkVersion = 10
		)
	}

	@Test fun `can override minSdkVersion (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.minSdkVersion = 10
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release"),
			minSdkVersion = 10
		)
	}

	@Test fun `can override targetSdkVersion (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.targetSdkVersion = 19
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug"),
			targetSdkVersion = 19
		)
	}

	@Test fun `can override targetSdkVersion (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.targetSdkVersion = 19
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release"),
			targetSdkVersion = 19
		)
	}

	@Test fun `can override compileSdkVersion (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.compileSdkVersion = 23
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug"),
			compileSdkVersion = 23
			//compileSdkVersionName = "6.0-2704002"
		)
	}

	@Test fun `can override compileSdkVersion (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.compileSdkVersion = 23
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release"),
			compileSdkVersion = 23
			//compileSdkVersionName = "6.0-2704002"
		)
	}

	@Test fun `adds custom resources and BuildConfig values`() {
		@Language("kotlin")
		val kotlinTestClass = """
			import ${packageName}.BuildConfig
			import ${packageName}.R

			@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
			class ResourceTest {
				@Suppress("USELESS_CAST") // validate the type and nullity of values
				@org.junit.Test fun test() { // using Robolectric to access resources at runtime
					val res = org.robolectric.RuntimeEnvironment.application.resources
					printProperty("in_prod=" + res.getBoolean(R.bool.in_prod) as Boolean)
					printProperty("in_test=" + res.getBoolean(R.bool.in_test) as Boolean)
					printProperty("app_package=" + res.getString(R.string.app_package) as String)
					printProperty("EMAIL=" + BuildConfig.EMAIL as String)
					printProperty("REVISION=" + BuildConfig.REVISION as String)
					printProperty("REVISION_NUMBER=" + BuildConfig.REVISION_NUMBER as Int)
					printProperty("BUILD_TIME=" + (BuildConfig.BUILD_TIME as java.util.Date).time)
				}
				private fun printProperty(prop: String) = println(BuildConfig.BUILD_TYPE + "." + prop)
			}

		""".trimIndent()
		gradle.file(kotlinTestClass, "src/test/kotlin/test.kt")

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			apply plugin: 'net.twisterrob.kotlin'
			dependencies {
				testImplementation 'junit:junit:4.13'
				testImplementation 'org.robolectric:robolectric:4.1'
			}
			// TODO AGP 3.3+ android.enableUnitTestBinaryResources=true crashes: https://issuetracker.google.com/issues/120098460
			android.testOptions.unitTests.includeAndroidResources = true
			tasks.withType(Test) {
				//noinspection UnnecessaryQualifiedReference
				testLogging.events = org.gradle.api.tasks.testing.logging.TestLogEvent.values().toList().toSet()
			}
		""".trimIndent()

		val today = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
		val result = gradle.run(script, "test").build()
		result.assertSuccess(":testReleaseUnitTest")
		result.assertHasOutputLine("    release.app_package=${packageName}")
		result.assertHasOutputLine("    release.in_prod=true")
		result.assertHasOutputLine("    release.in_test=false")
		result.assertHasOutputLine("    release.EMAIL=feedback@twisterrob.net")
		result.assertHasOutputLine("    release.REVISION=no VCS")
		result.assertHasOutputLine("    release.REVISION_NUMBER=0")
		result.assertHasOutputLine("    release.BUILD_TIME=${today}")
		result.assertSuccess(":testDebugUnitTest")
		result.assertHasOutputLine("    debug.app_package=${packageName}.debug")
		result.assertHasOutputLine("    debug.in_prod=false")
		result.assertHasOutputLine("    debug.in_test=true")
		result.assertHasOutputLine("    debug.EMAIL=papp.robert.s@gmail.com")
		result.assertHasOutputLine("    debug.REVISION=no VCS")
		result.assertHasOutputLine("    debug.REVISION_NUMBER=0")
		result.assertHasOutputLine("    debug.BUILD_TIME=${today}")
	}

	@Test fun `can customize build time`() {
		@Language("kotlin")
		val kotlinTestClass = """
			import ${packageName}.BuildConfig
			import ${packageName}.R

			@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
			class ResourceTest {
				@Suppress("USELESS_CAST") // validate the type and nullity of values
				@org.junit.Test fun test() { // using Robolectric to access resources at runtime
					printProperty("BUILD_TIME=" + (BuildConfig.BUILD_TIME as java.util.Date).time)
				}
				private fun printProperty(prop: String) = println(BuildConfig.BUILD_TYPE + "." + prop)
			}

		""".trimIndent()
		gradle.file(kotlinTestClass, "src/test/kotlin/test.kt")

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			apply plugin: 'net.twisterrob.kotlin'
			dependencies {
				testImplementation 'junit:junit:4.13'
				testImplementation 'org.robolectric:robolectric:4.1'
			}
			// TODO AGP 3.3+ android.enableUnitTestBinaryResources=true crashes: https://issuetracker.google.com/issues/120098460
			android.testOptions.unitTests.includeAndroidResources = true
			tasks.withType(Test) {
				//noinspection UnnecessaryQualifiedReference
				testLogging.events = org.gradle.api.tasks.testing.logging.TestLogEvent.values().toList().toSet()
			}
			tasks.decorateBuildConfig.configure { getBuildTime = { 1234567890 }}
		""".trimIndent()

		val result = gradle.run(script, "testReleaseUnitTest").build()

		result.assertSuccess(":testReleaseUnitTest")
		result.assertHasOutputLine("    release.BUILD_TIME=1234567890")
	}

	/**
	 * @see AndroidBuildPlugin.fixVariantTaskGroups
	 */
	@Test fun `metadata of compilation tasks is present`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, "tasks").build()

		result.assertHasOutputLine("""^compileDebugSources - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileReleaseSources - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileDebugJavaWithJavac - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileReleaseJavaWithJavac - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileDebugUnitTestSources - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileReleaseUnitTestSources - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileDebugUnitTestJavaWithJavac - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileReleaseUnitTestJavaWithJavac - (.+)""".toRegex())
		result.assertHasOutputLine("""^compileDebugAndroidTestSources - (.+)""".toRegex())
		result.assertNoOutputLine("""^compileReleaseAndroidTestSources -(.*)""".toRegex())
		result.assertHasOutputLine("""^compileDebugAndroidTestJavaWithJavac - (.+)""".toRegex())
		result.assertNoOutputLine("""^compileReleaseAndroidTestJavaWithJavac -(.*)""".toRegex())
	}
}
