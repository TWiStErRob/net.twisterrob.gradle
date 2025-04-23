package net.twisterrob.gradle.settings

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see SettingsPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class SettingsPluginIntgTest : BaseIntgTest() {
	override lateinit var gradle: GradleRunnerRule

	@Test fun `applying by the old name is deprecated`() {
		@Language("gradle.kts")
		val settings = """
			plugins {
				id("net.twisterrob.settings")
			}
		""".trimIndent()
		gradle.file(settings, "settings.gradle.kts")

		val result = gradle.run("").buildAndFail()

		result.assertHasOutputLine(
			Regex(
				"""org\.gradle\.api\.GradleException: """ +
						"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d+\.0"""
			)
		)
		result.assertHasOutputLine(
			Regex(
				"""The net\.twisterrob\.settings plugin has been deprecated\. """
						+ """This is scheduled to be removed in Gradle \d+\.0\. """
						+ """Please use the net\.twisterrob\.gradle\.plugin\.settings plugin instead."""
			)
		)
	}
}
