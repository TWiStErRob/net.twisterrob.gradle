package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.root
import net.twisterrob.gradle.vcs.createTestFileToCommit
import net.twisterrob.gradle.vcs.doCheckout
import net.twisterrob.gradle.vcs.doCommitSingleFile
import net.twisterrob.gradle.vcs.doCreateRepository
import net.twisterrob.gradle.vcs.svn
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
			apk = gradle.root.apk("debug")
		)
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release")
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
			apk = gradle.root.apk("debug", "${packageName}.debug@1234-vnull+debug.apk"),
			versionCode = "1234",
			versionName = ""
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
			apk = gradle.root.apk("release", "${packageName}@1234-vnull+release.apk"),
			versionCode = "1234",
			versionName = ""
		)
	}

	@Test fun `can give versionCode androidTest (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.versionCode = 1234
			android.defaultConfig.version.autoVersion = false
		""".trimIndent()

		val result = gradle.run(script, "assembleDebugAndroidTest").build()

		result.assertSuccess(":assembleDebugAndroidTest")
		assertDefaultBadging(
			applicationId = "${packageName}.debug.test",
			apk = gradle.root.apk("androidTest/debug", "${packageName}.debug.test@1234-vnull+debug-androidTest.apk"),
			versionCode = "1234",
			versionName = "",
			isAndroidTestApk = true
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
			apk = gradle.root.apk("debug", "${packageName}.debug@-1-v_custom_d+debug.apk"),
			versionCode = "",
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
			apk = gradle.root.apk("release", "${packageName}@-1-v_custom_+release.apk"),
			versionCode = "",
			versionName = "_custom_"
		)
	}

	@Test fun `can give versionName androidTest (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.versionName = "_custom_"
			android.defaultConfig.version.autoVersion = false
		""".trimIndent()

		val result = gradle.run(script, "assembleDebugAndroidTest").build()

		result.assertSuccess(":assembleDebugAndroidTest")
		assertDefaultBadging(
			applicationId = "${packageName}.debug.test",
			apk = gradle.root.apk("androidTest/debug", "${packageName}.debug.test@-1-v_custom_d+debug-androidTest.apk"),
			versionCode = "",
			versionName = "_custom_d",
			isAndroidTestApk = true
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

	@Test fun `can customize version propagates androidTest (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()

		val result = gradle.run(script, "assembleDebugAndroidTest").build()

		result.assertSuccess(":assembleDebugAndroidTest")
		assertDefaultBadging(
			applicationId = "${packageName}.debug.test",
			apk = gradle.root.apk(
				"androidTest/debug",
				"${packageName}.debug.test@10203004-v1.2.3#4d+debug-androidTest.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4d",
			isAndroidTestApk = true
		)
	}

	@Test fun `can customize version without rename (debug)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { renameAPK = false; major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()
		val projectName = "gradle-test-project"
		gradle.settingsFile.appendText("rootProject.name = '$projectName'")

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
		gradle.settingsFile.appendText("rootProject.name = '$projectName'")

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${projectName}-release-unsigned.apk"),
			versionCode = "10203004",
			versionName = "1.2.3#4"
		)
	}

	@Test fun `build version is used from SVN revision number (release)`() {
		svn {
			val repoUrl = doCreateRepository(gradle.root.resolve(".repo"))
			doCheckout(repoUrl, gradle.root)
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Commit 1")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Commit 2")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Commit 3")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Commit 4")
		}

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			android.defaultConfig.version { major = 1; minor = 2; patch = 3/*; build = 4*/ }
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@12300004-v1.2.3#4+release.apk"),
			versionCode = "12300004",
			versionName = "1.2.3#4"
		)
	}
}
