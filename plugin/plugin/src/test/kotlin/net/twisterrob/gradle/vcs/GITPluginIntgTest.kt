package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.root
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * @see GITPlugin
 * @see GITPluginExtension
 */
class GITPluginIntgTest : BaseIntgTest() {

	@Test fun `git is auto-selected when the working copy is GIT`() {
		git(gradle.root) {
			// empty repo
		}
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.vcs'
			println("VCS.current: " + project.VCS.current)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""VCS.current: extension '${GITPluginExtension.NAME}'""".toRegex())
	}

	@Test fun `git revision detected correctly`() {
		git(gradle.root) {
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "First commit")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Second commit")
		}
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.vcs'
			println("GIT revision: " + project.VCS.git.revision)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""GIT revision: [a-z0-9]{7}""".toRegex())
	}

	@Test fun `git revision number detected correctly`() {
		git(gradle.root) {
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "First commit")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Second commit")
		}
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.vcs'
			println("GIT revision: " + project.VCS.git.revisionNumber)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""GIT revision: 2""")
	}
}
