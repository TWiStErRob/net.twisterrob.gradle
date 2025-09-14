package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see AndroidLibraryPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidLibraryPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `applying by the old name is deprecated`() {
		// Default build.gradle has the app plugin applied.
		gradle.buildFile.writeText(gradle.buildFile.readText().replace("id(\"com.android.application\")", ""))

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.android-library")
			}
		""".trimIndent()

		val result = gradle.run(script).buildAndFail()

		result.assertHasOutputLine(
			Regex(
				"""org\.gradle\.api\.GradleException: """ +
						"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d+(\.0)?"""
			)
		)
		result.assertHasOutputLine(
			Regex(
				"""The net\.twisterrob\.android-library plugin has been deprecated\. """
						+ """This is scheduled to be removed in Gradle \d+(\.0)?\. """
						+ """Please use the net\.twisterrob\.gradle\.plugin\.android-library plugin instead."""
			)
		)
	}
}
