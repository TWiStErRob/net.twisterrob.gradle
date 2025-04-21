package net.twisterrob.gradle.vcs.svn

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.root
import net.twisterrob.gradle.vcs.createTestFileToCommit
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see SvnPlugin
 * @see SvnPluginExtension
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class SvnPluginIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `svn is auto-selected when the working copy is SVN`() {
		svn {
			val repoUrl = doCreateRepository(gradle.root.resolve(".repo"))
			doCheckout(repoUrl, gradle.root)
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

		result.assertHasOutputLine("""VCS.current: extension '${SvnPluginExtension.NAME}'""".toRegex())
	}

	@Test fun `svn revision detected correctly`() {
		svn {
			val repoUrl = doCreateRepository(gradle.root.resolve(".repo"))
			doCheckout(repoUrl, gradle.root)
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "First commit")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Second commit")
		}
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.vcs")
			}
			println("SVN revision: " + project.VCS.current.revision)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""SVN revision: 2""")
	}

	@Test fun `svn revision number detected correctly`() {
		svn {
			val repoUrl = doCreateRepository(gradle.root.resolve(".repo"))
			doCheckout(repoUrl, gradle.root)
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "First commit")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Second commit")
		}
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.vcs")
			}
			println("SVN revision: " + project.VCS.current.revisionNumber)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""SVN revision: 2""")
	}
}
