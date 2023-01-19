package net.twisterrob.gradle.android

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see AndroidAppPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class AndroidAppPluginIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `applying by the old name is deprecated`() {
		if (gradle.gradleVersion.baseVersion < GradleVersion.version("6.3")) {
			val result = gradle.run("apply plugin: 'net.twisterrob.android-app'").build()
			result.assertHasOutputLine("Plugin net.twisterrob.android-app is deprecated, please use net.twisterrob.gradle.plugin.android-app instead.")
		} else {
			val result = gradle.run("apply plugin: 'net.twisterrob.android-app'").buildAndFail()
			result.assertHasOutputLine(
				Regex(
					"""org\.gradle\.api\.GradleException: """ +
							"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d.0"""
				)
			)
			result.assertHasOutputLine(
				Regex(
					"""The net\.twisterrob\.android-app plugin has been deprecated\. """
							+ """This is scheduled to be removed in Gradle \d\.0\. """
							+ """Please use the net\.twisterrob\.gradle\.plugin\.android-app plugin instead."""
				)
			)
		}
	}
}
