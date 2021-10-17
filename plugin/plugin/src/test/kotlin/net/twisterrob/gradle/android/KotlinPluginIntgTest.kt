package net.twisterrob.gradle.android

import net.twisterrob.gradle.kotlin.KotlinPlugin
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.test.compile.generateKotlinCompilationCheck
import net.twisterrob.test.compile.generateKotlinCompilationCheckTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * [KotlinPlugin] interop with Android modules
 * @see KotlinPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class KotlinPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `can test kotlin with JUnit in Android Library`() {
		gradle.basedOn("kotlin-plugin_app")
		gradle.generateKotlinCompilationCheck()
		gradle.generateKotlinCompilationCheckTest()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-library'
			apply plugin: 'net.twisterrob.kotlin'
			dependencies {
				testImplementation "junit:junit:4.13.1"
			}
		""".trimIndent()

		val result = gradle.run(script, "compileDebugUnitTestSources").build()

		result.assertSuccess(":compileDebugKotlin")
		result.assertSuccess(":compileDebugUnitTestKotlin")
	}

	@Test fun `can test kotlin with JUnit in Android App`() {
		gradle.basedOn("kotlin-plugin_app")
		gradle.generateKotlinCompilationCheck()
		gradle.generateKotlinCompilationCheckTest()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
			apply plugin: 'net.twisterrob.kotlin'
			dependencies {
				testImplementation "junit:junit:4.13.1"
			}
		""".trimIndent()

		val result = gradle.run(script, "compileDebugUnitTestSources").build()

		result.assertSuccess(":compileDebugKotlin")
		result.assertSuccess(":compileDebugUnitTestKotlin")
	}

	@Test fun `can test kotlin with JUnit in Android Test App`() {
		gradle.basedOn("kotlin-plugin_app")
		gradle.generateKotlinCompilationCheck("test")
		gradle.generateKotlinCompilationCheckTest("test")

		gradle.settingsFile.writeText("include ':test'")
		@Language("gradle")
		val appScript = """
			apply plugin: 'net.twisterrob.android-test'
			apply plugin: 'net.twisterrob.kotlin'
			dependencies {
				implementation "junit:junit:4.13.1"
			}
			android.targetProjectPath = ':'
		"""
		gradle.file(appScript, "test", "build.gradle")
		@Language("xml")
		val androidManifest = """
			<manifest package="${packageName}.test" />
		""".trimIndent()
		gradle.file(androidManifest, "test/src/main/AndroidManifest.xml")

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.android-app'
		""".trimIndent()

		val result = gradle.run(script, ":test:compileDebugSources").build()

		result.assertSuccess(":test:compileDebugKotlin")
	}
}
