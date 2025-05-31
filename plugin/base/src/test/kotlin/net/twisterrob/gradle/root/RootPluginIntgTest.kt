package net.twisterrob.gradle.root

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.fqcn
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see RootPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class RootPluginIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `applies GradlePlugin`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.root")
			}
			
			plugins.withType(${GradlePlugin::class.fqcn}) {
				println("Gradle Plugin applied")
			}
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("Gradle Plugin applied")
	}

	@Test fun `applying by the old name is deprecated`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.root")
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
				"""The net\.twisterrob\.root plugin has been deprecated\. """
						+ """This is scheduled to be removed in Gradle \d+\.0\. """
						+ """Please use the net\.twisterrob\.gradle\.plugin\.root plugin instead."""
			)
		)
	}
}
