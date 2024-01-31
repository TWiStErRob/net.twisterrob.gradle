package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.root
import net.twisterrob.gradle.vcs.git.git
import net.twisterrob.gradle.vcs.svn.SVNPluginExtension
import net.twisterrob.gradle.vcs.svn.doCheckout
import net.twisterrob.gradle.vcs.svn.doCreateRepository
import net.twisterrob.gradle.vcs.svn.svn
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see VCSPlugin
 * @see VCSPluginExtension
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class VCSPluginIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `git is auto-selected when the working copy is both SVN and GIT`() {
		svn {
			val repoUrl = doCreateRepository(gradle.root.resolve(".repo"))
			doCheckout(repoUrl, gradle.root)
			// empty repo
		}
		git(gradle.root) {
			// empty repo
		}
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.vcs")
			}
			println("VCS.current: " + project.VCS.current)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""VCS.current: extension '${SVNPluginExtension.NAME}'""".toRegex())
	}

	@Test fun `applying by the old name is deprecated`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.vcs")
			}
		""".trimIndent()

		val result = gradle.run(script).buildAndFail()

		result.assertHasOutputLine(
			Regex(
				"""org\.gradle\.api\.GradleException: """ +
						"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d.0"""
			)
		)
		result.assertHasOutputLine(
			Regex(
				"""The net\.twisterrob\.vcs plugin has been deprecated\. """
						+ """This is scheduled to be removed in Gradle \d\.0\. """
						+ """Please use the net\.twisterrob\.gradle\.plugin\.vcs plugin instead."""
			)
		)
	}
}
