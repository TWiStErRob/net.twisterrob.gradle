package net.twisterrob.gradle.kotlin

import junit.runner.Version
import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleBuildTestResources
import net.twisterrob.gradle.test.GradleBuildTestResources.basedOn
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.test.compile.generateKotlinCompileCheckMain
import net.twisterrob.test.compile.generateKotlinCompileCheckTest
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
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.generateKotlinCompileCheckMain()

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.kotlin")
			}
		""".trimIndent()

		val result = gradle.run(script, "jar").build()

		result.assertSuccess(":compileKotlin")
	}

	@Test fun `can test Kotlin with TestNG`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.generateKotlinCompileCheckMain()
		gradle.generateKotlinCompileCheckTest()

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.kotlin")
			}
			test {
				useTestNG()
			}
			dependencies {
				testImplementation("org.testng:testng:7.7.1")
			}
		""".trimIndent()

		val result = gradle.run(script, "test").build()

		result.assertSuccess(":compileKotlin")
		result.assertSuccess(":compileTestKotlin")
	}

	@Test fun `can test Kotlin with JUnit`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.generateKotlinCompileCheckMain()
		gradle.generateKotlinCompileCheckTest()

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.kotlin")
			}
			dependencies {
				testImplementation("junit:junit:${Version.id()}")
			}
		""".trimIndent()

		val result = gradle.run(script, "test").build()

		result.assertSuccess(":compileKotlin")
		result.assertSuccess(":compileTestKotlin")
	}

	@Test fun `does not add repositories when it would fail`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		// Need "some" source code to force resolving dependencies.
		gradle.file("", "src", "main", "java", "triggerCompilation.kt")

		@Language("gradle")
		val settings = """
			dependencyResolutionManagement {
				repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
			}
		""".trimIndent()
		gradle.settingsFile.writeText(settings)

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.kotlin")
			}
		""".trimIndent()

		val result = gradle.run(script, "jar").buildAndFail()

		result.assertNoOutputLine(
			"""
				.*Build was configured to prefer settings repositories over project repositories but repository 'MavenRepo' was added by plugin 'net\.twisterrob\.kotlin'
			""".trimIndent().toRegex()
		)
		result.assertNoOutputLine(
			"""
				.*Build was configured to prefer settings repositories over project repositories but repository '.*' was added by plugin '.*'
			""".trimIndent().toRegex()
		)
		result.assertHasOutputLine(
			"""
				.*Cannot resolve external dependency (.*) because no repositories are defined\.
			""".trimIndent().toRegex()
		)
	}

	@Test fun `applying by the old name is deprecated`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.kotlin")
			}
		""".trimIndent()

		val result = gradle.run(script).buildAndFail()

		result.assertHasOutputLine(
			Regex(
				"""org\.gradle\.api\.GradleException: """ +
						"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d+\.0"""
			)
		)
		result.assertHasOutputLine(
			Regex(
				"""The net\.twisterrob\.kotlin plugin has been deprecated\. """
						+ """This is scheduled to be removed in Gradle \d+\.0\. """
						+ """Please use the net\.twisterrob\.gradle\.plugin\.kotlin plugin instead."""
			)
		)
	}
}
