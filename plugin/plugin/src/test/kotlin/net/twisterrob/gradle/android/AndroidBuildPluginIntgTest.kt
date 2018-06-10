package net.twisterrob.gradle.android

import org.intellij.lang.annotations.Language
import org.junit.Test

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
			compileSdkVersionName = "6.0-2704002"
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
			compileSdkVersionName = "6.0-2704002"
		)
	}
}
