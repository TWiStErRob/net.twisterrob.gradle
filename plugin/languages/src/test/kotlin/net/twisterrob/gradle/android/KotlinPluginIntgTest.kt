package net.twisterrob.gradle.android

import junit.runner.Version
import net.twisterrob.gradle.kotlin.KotlinPlugin
import net.twisterrob.gradle.test.GradleBuildTestResources
import net.twisterrob.gradle.test.GradleBuildTestResources.basedOn
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.test.compile.generateKotlinCompileCheckMain
import net.twisterrob.test.compile.generateKotlinCompileCheckTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Test [KotlinPlugin] interop with Android modules.
 *
 * @see KotlinPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class KotlinPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `can test kotlin with JUnit in Android Library`() {
		// Default build.gradle has the app plugin applied.
		gradle.buildFile.writeText(gradle.buildFile.readText().replace("id(\"com.android.application\")", ""))
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.generateKotlinCompileCheckMain()
		gradle.generateKotlinCompileCheckTest()

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-library")
				id("net.twisterrob.gradle.plugin.kotlin")
			}
			dependencies {
				testImplementation("junit:junit:${Version.id()}")
			}
		""".trimIndent()

		val result = gradle.run(script, "compileDebugUnitTestSources").build()

		result.assertSuccess(":compileDebugKotlin")
		result.assertSuccess(":compileDebugUnitTestKotlin")
	}

	@Test fun `can test kotlin with JUnit in Android App`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.generateKotlinCompileCheckMain()
		gradle.generateKotlinCompileCheckTest()

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
				id("net.twisterrob.gradle.plugin.kotlin")
			}
			dependencies {
				testImplementation("junit:junit:${Version.id()}")
			}
		""".trimIndent()

		val result = gradle.run(script, "compileDebugUnitTestSources").build()

		result.assertSuccess(":compileDebugKotlin")
		result.assertSuccess(":compileDebugUnitTestKotlin")
	}

	@Test fun `can test kotlin with JUnit in Android Test App`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.generateKotlinCompileCheckMain("test")
		gradle.generateKotlinCompileCheckTest("test")

		gradle.settingsFile.appendText("""include(":test")${System.lineSeparator()}""")
		@Language("gradle")
		val appScript = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-test")
				id("net.twisterrob.gradle.plugin.kotlin")
			}
			dependencies {
				implementation("junit:junit:${Version.id()}")
			}
			android.namespace = "${packageName}.test"
			android.targetProjectPath = ':'
			android.compileSdkVersion = "${System.getProperty("net.twisterrob.test.android.compileSdkVersion")}"
		""".trimIndent()
		gradle.file(appScript, "test", "build.gradle")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
			}
		""".trimIndent()

		val result = gradle.run(script, ":test:compileDebugSources").build()

		result.assertSuccess(":test:compileDebugKotlin")
	}
}
