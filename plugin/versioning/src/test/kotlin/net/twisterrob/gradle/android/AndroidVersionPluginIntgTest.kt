package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.root
import net.twisterrob.gradle.vcs.createTestFileToCommit
import net.twisterrob.gradle.vcs.git.doCommitSingleFile
import net.twisterrob.gradle.vcs.git.git
import net.twisterrob.gradle.vcs.svn.doCheckout
import net.twisterrob.gradle.vcs.svn.doCommitSingleFile
import net.twisterrob.gradle.vcs.svn.doCreateRepository
import net.twisterrob.gradle.vcs.svn.svn
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see AndroidVersionPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidVersionPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `can use version block (debug) and (release)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
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
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.versionCode = 1234
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug", "${packageName}.debug@1234-v+debug.apk"),
			versionCode = "1234",
			versionName = ""
		)
	}

	@Test fun `can give versionCode (release)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.versionCode = 1234
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@1234-v+release.apk"),
			versionCode = "1234"
		)
	}

	@Test fun `can give versionCode androidTest (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.versionCode = 1234
		""".trimIndent()

		val result = gradle.run(script, "assembleDebugAndroidTest").build()

		result.assertSuccess(":assembleDebugAndroidTest")
		assertDefaultBadging(
			applicationId = "${packageName}.debug.test",
			apk = gradle.root.apk("androidTest/debug", "${packageName}.debug.test@1234-v+debug-androidTest.apk"),
			versionCode = "1234",
			versionName = "",
			isAndroidTestApk = true
		)
	}

	@Test fun `can give versionName (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.versionName = "_custom_"
		""".trimIndent()

		val result = gradle.run(script, "assembleDebug").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug", "${packageName}.debug@-1-v_custom_d+debug.apk"),
			versionName = "_custom_d"
		)
	}

	@Test fun `can give versionName (release)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.versionName = "_custom_"
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@-1-v_custom_+release.apk"),
			versionName = "_custom_"
		)
	}

	@Test fun `can give versionName androidTest (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.versionName = "_custom_"
		""".trimIndent()

		val result = gradle.run(script, "assembleDebugAndroidTest").build()

		result.assertSuccess(":assembleDebugAndroidTest")
		assertDefaultBadging(
			applicationId = "${packageName}.debug.test",
			apk = gradle.root.apk("androidTest/debug", "${packageName}.debug.test@-1-v_custom_d+debug-androidTest.apk"),
			versionName = "_custom_d",
			isAndroidTestApk = true
		)
	}

	@Test fun `can customize version (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
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
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
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

	@Test fun `can turn off autoVersion before setting versions (debug) and (release)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version.autoVersion = false
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()

		val result = gradle.run(script, "assemble").build()

		result.assertSuccess(":assembleDebug")
		assertDefaultDebugBadging(
			apk = gradle.root.apk("debug")
		)

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release")
		)
	}

	@Test fun `can customize version propagates androidTest (debug)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
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
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { renameAPK = false; major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()
		val projectName = "gradle-test-project"
		gradle.settingsFile.appendText("""rootProject.name = "${projectName}"""")

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
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { renameAPK = false; major = 1; minor = 2; patch = 3; build = 4 }
		""".trimIndent()
		val projectName = "gradle-test-project"
		gradle.settingsFile.appendText("""rootProject.name = "${projectName}"""")

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${projectName}-release-unsigned.apk"),
			versionCode = "10203004",
			versionName = "1.2.3#4"
		)
	}

	@Test fun `autoVersion turns on when version properties file exists and overrides defaultConfig`() {
		@Language("properties")
		val versionProperties = """
			# Since AGP 3.3 versionCode must be > 0
			major=1
			minor=2
			patch=3
			build=4
		""".trimIndent()
		gradle.file(versionProperties, "version.properties")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.versionCode = 6789
			android.defaultConfig.versionName = "_custom_"
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@10203004-v1.2.3#4+release.apk"),
			versionCode = "10203004",
			versionName = "1.2.3#4"
		)
	}

	@Test fun `autoVersion turns on when version properties exists, but fails due to no values set`() {
		@Language("properties")
		val versionProperties = """
			# Empty file without versions.
		""".trimIndent()
		gradle.file(versionProperties, "version.properties")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").buildAndFail()

		result.assertHasOutputLine(
			"""
				.*android\.defaultConfig\.versionCode is set to 0, but it should be a positive integer\..*
			""".trimIndent().toRegex()
		)
		result.assertHasOutputLine(
			"""
				.*See https://developer\.android\.com/studio/publish/versioning#appversioning for more information\..*
			""".trimIndent().toRegex()
		)
	}

	@Test fun `autoVersion can be turned off even when the file exists`() {
		@Language("properties")
		val versionProperties = """
			major=1
			minor=2
			patch=3
			build=4
		""".trimIndent()
		gradle.file(versionProperties, "version.properties")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version.autoVersion = false
			android.defaultConfig.versionName = "_custom_"
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@-1-v_custom_+release.apk"),
			versionName = "_custom_"
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
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
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

	@Test fun `build version is used from GIT revision number (release)`() {
		lateinit var rev: String
		git(gradle.root) {
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Commit 1")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Commit 2")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Commit 3")
			rev = doCommitSingleFile(gradle.root.createTestFileToCommit(), "Commit 4").abbreviate(7).name()
		}

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { major = 1; minor = 2; patch = 3/*; build = 4*/ }
		""".trimIndent()

		val result = gradle.run(script, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
		assertDefaultReleaseBadging(
			apk = gradle.root.apk("release", "${packageName}@12300004-v1.2.3#4-${rev}+release.apk"),
			versionCode = "12300004",
			versionName = "1.2.3#4-${rev}"
		)
	}

	@Suppress("detekt.LongMethod") // Variants are fun, aren't they.
	@Test fun `variant versioning works`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
			android.defaultConfig.version { major = 1; minor = 2; patch = 3; build = 4 }
			android {
				testBuildType = "staging"
				buildTypes {
					staging {
						initWith debug
						applicationIdSuffix ".staging"
						versionNameSuffix "S"
					}
				}
				flavorDimensions = ["cost", "version"]
				productFlavors {
					demo {
						dimension "version"
						applicationIdSuffix ".demo"
						versionNameSuffix "-demo"
					}
					full {
						dimension "version"
						applicationIdSuffix ".full"
						versionNameSuffix "-full"
					}
					paid {
						dimension "cost"
						applicationIdSuffix ".paid"
						versionNameSuffix "-paid"
					}
					free {
						dimension "cost"
						applicationIdSuffix ".free"
						versionNameSuffix "-free"
					}
				}
			}
		""".trimIndent()

		val result = gradle.run(script, "assemble", "assembleAndroidTest").build()

		result.assertNoOutputLine(Regex(""".*Gradle detected a problem with the following location.*"""))
		result.assertSuccess(":assemblePaidDemoDebug")
		result.assertSuccess(":assembleFreeDemoDebug")
		result.assertSuccess(":assemblePaidFullDebug")
		result.assertSuccess(":assembleFreeFullDebug")
		assertDefaultDebugBadging(
			applicationId = "${packageName}.paid.demo.debug",
			apk = gradle.root.apk(
				"paidDemo/debug",
				"${packageName}.paid.demo.debug@10203004-v1.2.3#4-paid-demod+paidDemoDebug.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-paid-demod"
		)
		assertDefaultDebugBadging(
			applicationId = "${packageName}.free.demo.debug",
			apk = gradle.root.apk(
				"freeDemo/debug",
				"${packageName}.free.demo.debug@10203004-v1.2.3#4-free-demod+freeDemoDebug.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-free-demod"
		)
		assertDefaultDebugBadging(
			applicationId = "${packageName}.paid.full.debug",
			apk = gradle.root.apk(
				"paidFull/debug",
				"${packageName}.paid.full.debug@10203004-v1.2.3#4-paid-fulld+paidFullDebug.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-paid-fulld"
		)
		assertDefaultDebugBadging(
			applicationId = "${packageName}.free.full.debug",
			apk = gradle.root.apk(
				"freeFull/debug",
				"${packageName}.free.full.debug@10203004-v1.2.3#4-free-fulld+freeFullDebug.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-free-fulld"
		)

		result.assertSuccess(":assemblePaidDemoRelease")
		result.assertSuccess(":assembleFreeDemoRelease")
		result.assertSuccess(":assemblePaidFullRelease")
		result.assertSuccess(":assembleFreeFullRelease")
		assertDefaultReleaseBadging(
			applicationId = "${packageName}.paid.demo",
			apk = gradle.root.apk(
				"paidDemo/release",
				"${packageName}.paid.demo@10203004-v1.2.3#4-paid-demo+paidDemoRelease.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-paid-demo"
		)
		assertDefaultReleaseBadging(
			applicationId = "${packageName}.free.demo",
			apk = gradle.root.apk(
				"freeDemo/release",
				"${packageName}.free.demo@10203004-v1.2.3#4-free-demo+freeDemoRelease.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-free-demo"
		)
		assertDefaultReleaseBadging(
			applicationId = "${packageName}.paid.full",
			apk = gradle.root.apk(
				"paidFull/release",
				"${packageName}.paid.full@10203004-v1.2.3#4-paid-full+paidFullRelease.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-paid-full"
		)
		assertDefaultReleaseBadging(
			applicationId = "${packageName}.free.full",
			apk = gradle.root.apk(
				"freeFull/release",
				"${packageName}.free.full@10203004-v1.2.3#4-free-full+freeFullRelease.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-free-full"
		)

		result.assertSuccess(":assemblePaidDemoStaging")
		result.assertSuccess(":assembleFreeDemoStaging")
		result.assertSuccess(":assemblePaidFullStaging")
		result.assertSuccess(":assembleFreeFullStaging")
		assertDefaultBadging(
			applicationId = "${packageName}.paid.demo.staging",
			apk = gradle.root.apk(
				"paidDemo/staging",
				"${packageName}.paid.demo.staging@10203004-v1.2.3#4-paid-demoS+paidDemoStaging.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-paid-demoS"
		)
		assertDefaultBadging(
			applicationId = "${packageName}.free.demo.staging",
			apk = gradle.root.apk(
				"freeDemo/staging",
				"${packageName}.free.demo.staging@10203004-v1.2.3#4-free-demoS+freeDemoStaging.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-free-demoS"
		)
		assertDefaultBadging(
			applicationId = "${packageName}.paid.full.staging",
			apk = gradle.root.apk(
				"paidFull/staging",
				"${packageName}.paid.full.staging@10203004-v1.2.3#4-paid-fullS+paidFullStaging.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-paid-fullS"
		)
		assertDefaultBadging(
			applicationId = "${packageName}.free.full.staging",
			apk = gradle.root.apk(
				"freeFull/staging",
				"${packageName}.free.full.staging@10203004-v1.2.3#4-free-fullS+freeFullStaging.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-free-fullS"
		)

		result.assertSuccess(":assemblePaidDemoStagingAndroidTest")
		result.assertSuccess(":assembleFreeDemoStagingAndroidTest")
		result.assertSuccess(":assemblePaidFullStagingAndroidTest")
		result.assertSuccess(":assembleFreeFullStagingAndroidTest")
		assertDefaultBadging(
			applicationId = "${packageName}.paid.demo.staging.test",
			apk = gradle.root.apk(
				"androidTest/paidDemo/staging",
				"${packageName}.paid.demo.staging.test@10203004-v1.2.3#4-paid-demoS+paidDemoStaging-androidTest.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-paid-demoS",
			isAndroidTestApk = true
		)
		assertDefaultBadging(
			applicationId = "${packageName}.free.demo.staging.test",
			apk = gradle.root.apk(
				"androidTest/freeDemo/staging",
				"${packageName}.free.demo.staging.test@10203004-v1.2.3#4-free-demoS+freeDemoStaging-androidTest.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-free-demoS",
			isAndroidTestApk = true
		)
		assertDefaultBadging(
			applicationId = "${packageName}.paid.full.staging.test",
			apk = gradle.root.apk(
				"androidTest/paidFull/staging",
				"${packageName}.paid.full.staging.test@10203004-v1.2.3#4-paid-fullS+paidFullStaging-androidTest.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-paid-fullS",
			isAndroidTestApk = true
		)
		assertDefaultBadging(
			applicationId = "${packageName}.free.full.staging.test",
			apk = gradle.root.apk(
				"androidTest/freeFull/staging",
				"${packageName}.free.full.staging.test@10203004-v1.2.3#4-free-fullS+freeFullStaging-androidTest.apk"
			),
			versionCode = "10203004",
			versionName = "1.2.3#4-free-fullS",
			isAndroidTestApk = true
		)
	}
}
