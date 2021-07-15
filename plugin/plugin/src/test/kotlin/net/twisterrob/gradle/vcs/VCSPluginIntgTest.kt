package net.twisterrob.gradle.vcs

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.root
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

/**
 * @see VCSPlugin
 * @see VCSPluginExtension
 */
class VCSPluginIntgTest : BaseIntgTest() {

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
			apply plugin: 'net.twisterrob.vcs'
			println("VCS.current: " + project.VCS.current)
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("""VCS.current: extension '${SVNPluginExtension.NAME}'""".toRegex())
	}
}
