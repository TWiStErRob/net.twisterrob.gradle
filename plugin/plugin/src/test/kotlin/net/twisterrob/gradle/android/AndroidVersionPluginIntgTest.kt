package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.assertHasOutputLine
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * @see AndroidVersionPlugin
 */
class AndroidVersionPluginIntgTest : BaseAndroidIntgTest() {

	@Test fun `can use version block (debug) and (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { println("version block!") }
		""".trimIndent()

		val result = gradle.run(script, "assemble").build()

		result.assertSuccess(":assembleDebug")
		result.assertSuccess(":assembleRelease")
		result.assertHasOutputLine("version block!")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug", "${packageName}.debug@0-v0.0.0#0d+debug.apk")
		)
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@0-v0.0.0#0+release.apk")
		)
	}

	@Test fun `can give versionCode (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.versionCode = 1234
			android.defaultConfig.version.autoVersion = false
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug", "${packageName}.debug@1234-v0.0.0#0d+debug.apk"),
			versionCode = "1234"
		)
	}

	@Test fun `can give versionCode (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.versionCode = 1234
			android.defaultConfig.version.autoVersion = false
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@1234-v0.0.0#0+release.apk"),
			versionCode = "1234"
		)
	}

	@Test fun `can give versionName (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.versionName = "_custom_"
			android.defaultConfig.version.autoVersion = false
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug", "${packageName}.debug@0-v_custom_d+debug.apk"),
			versionName = "_custom_d"
		)
	}

	@Test fun `can give versionName (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.versionName = "_custom_"
			android.defaultConfig.version.autoVersion = false
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@0-v_custom_+release.apk"),
			versionName = "_custom_"
		)
	}

	@Test fun `can customize version (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug", "${packageName}.debug@10203004-v1.2.3#4d+debug.apk"),
			versionCode = "10203004",
			versionName = "1.2.3#4d"
		)
	}

	@Test fun `can customize version (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@10203004-v1.2.3#4+release.apk"),
			versionCode = "10203004",
			versionName = "1.2.3#4"
		)
	}

	@Test fun `can customize version without rename (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { renameAPK = false; major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()
		val projectName = "gradle-test-project"
		gradle.settingsFile().appendText("rootProject.name = '$projectName'")

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug", "${projectName}-debug.apk"),
			versionCode = "10203004",
			versionName = "1.2.3#4d"
		)
	}

	@Test fun `can customize version without rename (release)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { renameAPK = false; major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()
		val projectName = "gradle-test-project"
		gradle.settingsFile().appendText("rootProject.name = '$projectName'")

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${projectName}-release-unsigned.apk"),
			versionCode = "10203004",
			versionName = "1.2.3#4"
		)
	}
}
