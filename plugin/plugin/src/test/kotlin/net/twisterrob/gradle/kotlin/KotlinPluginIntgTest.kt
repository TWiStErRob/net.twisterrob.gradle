package net.twisterrob.gradle.kotlin

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.test.compile.generateKotlinCompilationCheck
import net.twisterrob.test.compile.generateKotlinCompilationCheckTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see KotlinPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class KotlinPluginIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `can compile Kotlin`() {
		gradle.basedOn("kotlin-plugin_app")
		gradle.generateKotlinCompilationCheck()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.kotlin'
		""".trimIndent()

		val result = gradle.run(script, "jar").build()

		result.assertSuccess(":compileKotlin")
	}

	@Test fun `can test Kotlin with TestNG`() {
		gradle.basedOn("kotlin-plugin_app")
		gradle.generateKotlinCompilationCheck()
		gradle.generateKotlinCompilationCheckTest()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.kotlin'
			dependencies {
				testImplementation "org.testng:testng:6.14.3"
			}
		""".trimIndent()

		val result = gradle.run(script, "test").build()

		result.assertSuccess(":compileKotlin")
		result.assertSuccess(":compileTestKotlin")
	}

	@Test fun `can test Kotlin with JUnit`() {
		gradle.basedOn("kotlin-plugin_app")
		gradle.generateKotlinCompilationCheck()
		gradle.generateKotlinCompilationCheckTest()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.kotlin'
			dependencies {
				testImplementation "junit:junit:4.13.1"
			}
		""".trimIndent()

		val result = gradle.run(script, "test").build()

		result.assertSuccess(":compileKotlin")
		result.assertSuccess(":compileTestKotlin")
	}
}
