package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.root
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * @see SVNPlugin
 * @see SVNPluginExtension
 */
class SVNPluginIntgTest : BaseIntgTest() {

	@Test fun `svn is auto-selected when the working copy is SVN`() {
		svn {
			val repoUrl = doCreateRepository(gradle.root.resolve(".repo"))
			doCheckout(repoUrl, gradle.root)
		}
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.vcs'
			println(project.VCS.current)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine(""".*${SVNPluginExtension::class.qualifiedName}_Decorated@.*""".toRegex())
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
			apply plugin: 'net.twisterrob.vcs'
			println("SVN revision: " + project.VCS.svn.revision)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""SVN revision: 2""")
	}
}
