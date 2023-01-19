package net.twisterrob.gradle.settings

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import org.gradle.util.GradleVersion
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
		if (gradle.gradleVersion.baseVersion < GradleVersion.version("6.3")) {
			gradle.file(settings, "settings.gradle.kts")
			val result = gradle.run("").build()
			result.assertHasOutputLine(
				"Plugin net.twisterrob.settings is deprecated, " +
						"please use net.twisterrob.gradle.plugin.settings instead."
			)
		} else {
			gradle.file(settings, "settings.gradle.kts")

			val result = gradle.run("").buildAndFail()

			result.assertHasOutputLine(
				Regex(
					"""org\.gradle\.api\.GradleException: """ +
							"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d.0"""
				)
			)
			result.assertHasOutputLine(
				Regex(
					"""The net\.twisterrob\.settings plugin has been deprecated\. """
							+ """This is scheduled to be removed in Gradle \d\.0\. """
							+ """Please use the net\.twisterrob\.gradle\.plugin\.settings plugin instead."""
				)
			)
		}
	}
}
