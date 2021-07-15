package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.root
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

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
			println("GIT revision: " + project.VCS.current.revision)
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
			println("GIT revision: " + project.VCS.current.revisionNumber)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""GIT revision: 2""")
	}

	@Test fun `fails with malformed git directory`() {
		gradle.root.resolve(".git").mkdir()
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.vcs'
			println("VCS.current: " + project.VCS.current)
			println("GIT revision: " + project.VCS.current.revision)
		""".trimIndent()

		val result = gradle.run(script).buildAndFail()

		result.assertHasOutputLine("""VCS.current: extension '${GITPluginExtension.NAME}'""".toRegex())
		result.assertHasOutputLine("""> repository not found: \Q${gradle.root.absolutePath}\E""".toRegex())
	}

	/**
	 * Testing for https://bugs.eclipse.org/bugs/show_bug.cgi?id=572617
	 */
	@Test fun `git does not crash when config directory is present`() {
		// Must not have real repo in .git, because that would trigger quick check: `git(gradle.root) {}`.
		gradle.root.resolve("config").mkdir()
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.vcs'
			println("VCS.current: " + project.VCS.current)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""VCS.current: ${DummyVcsExtension::class.qualifiedName}@[a-z0-9]{1,8}""".toRegex())
	}
}
