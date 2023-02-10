package net.twisterrob.gradle.android

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertSuccess
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidSigningPluginIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `logs error when keystore not valid (release)`() {
		@Language("gradle")
		val build = """
			buildscript {
				repositories {
					google()
					mavenCentral()
				}
				dependencies {
					classpath 'com.android.tools.build:gradle:${System.getProperty("net.twisterrob.test.android.pluginVersion")}'
				}
			}
			repositories {
				google() // for aapt2 internal binary
				mavenCentral()
			}
			apply plugin: 'net.twisterrob.android-app'
			android {
				compileSdkVersion '${System.getProperty("net.twisterrob.test.android.compileSdkVersion")}'
			}
		""".trimIndent()
		gradle.file(build, "build.gradle")
		gradle.file("<manifest package=\"net.twisterrob.gradle.test_app\" />", "src", "main", "AndroidManifest.xml")

		@Language("java")
		val apkContentForProguard = """
			package ${packageName};
			class AClassToSatisfyProguard {
				@android.webkit.JavascriptInterface public void f() {}
			}
		""".trimIndent()
		gradle.file(apkContentForProguard, "src/main/java/${packageFolder}/AClassToSatisfyProguard.java")

		val result = gradle.run(null, "assembleRelease").build()

		result.assertSuccess(":assembleRelease")
	}
}
