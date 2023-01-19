package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see AndroidTestPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidTestPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `applying by the old name is deprecated`() {
		if (gradle.gradleVersion.baseVersion < GradleVersion.version("6.3")) {
			val result = gradle.run("apply plugin: 'net.twisterrob.android-test'").build()
			result.assertHasOutputLine(
				"Plugin net.twisterrob.android-test is deprecated, " +
						"please use net.twisterrob.gradle.plugin.android-test instead."
			)
		} else {
			val result = gradle.run("apply plugin: 'net.twisterrob.android-test'").buildAndFail()
			result.assertHasOutputLine(
				Regex(
					"""org\.gradle\.api\.GradleException: """ +
							"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d.0"""
				)
			)
			result.assertHasOutputLine(
				Regex(
					"""The net\.twisterrob\.android-test plugin has been deprecated\. """
							+ """This is scheduled to be removed in Gradle \d\.0\. """
							+ """Please use the net\.twisterrob\.gradle\.plugin\.android-test plugin instead."""
				)
			)
		}
	}
}
