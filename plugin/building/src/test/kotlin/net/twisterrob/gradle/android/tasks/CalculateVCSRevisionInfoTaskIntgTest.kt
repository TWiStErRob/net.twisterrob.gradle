package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.BaseAndroidIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.assertUpToDate
import net.twisterrob.gradle.test.root
import net.twisterrob.gradle.vcs.createTestFileToCommit
import net.twisterrob.gradle.vcs.doCheckout
import net.twisterrob.gradle.vcs.doCommitSingleFile
import net.twisterrob.gradle.vcs.doCreateRepository
import net.twisterrob.gradle.vcs.git
import net.twisterrob.gradle.vcs.svn
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see CalculateVCSRevisionInfoTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class CalculateVCSRevisionInfoTaskIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `will stay up to date when didn't change`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.vcs")
			}
			if (project.VCS.current == project.VCS.git || project.VCS.current == project.VCS.svn) {
				throw new IllegalStateException("Not dummy: " + project.VCS.current)
			}
			tasks.register("calculateBuildConfigVCSRevisionInfo", ${CalculateVCSRevisionInfoTask::class.java.name})
		""".trimIndent()

		val first = gradle.run(script, "calculateBuildConfigVCSRevisionInfo").build()
		first.assertSuccess(":calculateBuildConfigVCSRevisionInfo")

		val second = gradle.run(null, "calculateBuildConfigVCSRevisionInfo").build()
		second.assertUpToDate(":calculateBuildConfigVCSRevisionInfo")
	}

	@Test fun `will stay up to date when git didn't change`() {
		git(gradle.root) {
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "First commit")
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Second commit")
		}
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.vcs")
			}
			if (project.VCS.current != project.VCS.git) {
				throw new IllegalStateException("Not git: " + project.VCS.current)
			}
			tasks.register("calculateBuildConfigVCSRevisionInfo", ${CalculateVCSRevisionInfoTask::class.java.name})
		""".trimIndent()

		val first = gradle.run(script, "calculateBuildConfigVCSRevisionInfo").build()
		first.assertSuccess(":calculateBuildConfigVCSRevisionInfo")

		val second = gradle.run(null, "calculateBuildConfigVCSRevisionInfo").build()
		second.assertUpToDate(":calculateBuildConfigVCSRevisionInfo")
	}

	@Test fun `will stay up to date when git sha didn't change`() {
		git(gradle.root) {
			val rev = doCommitSingleFile(gradle.root.createTestFileToCommit(), "First commit")
			doCheckout(rev.id)
		}
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.vcs")
			}
			if (project.VCS.current != project.VCS.git) {
				throw new IllegalStateException("Not git: " + project.VCS.current)
			}
			tasks.register("calculateBuildConfigVCSRevisionInfo", ${CalculateVCSRevisionInfoTask::class.java.name})
		""".trimIndent()

		val first = gradle.run(script, "calculateBuildConfigVCSRevisionInfo").build()
		first.assertSuccess(":calculateBuildConfigVCSRevisionInfo")

		val second = gradle.run(null, "calculateBuildConfigVCSRevisionInfo").build()
		second.assertUpToDate(":calculateBuildConfigVCSRevisionInfo")
	}

	@Test fun `will not stay up to date when git changed`() {
		git(gradle.root) {
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "First commit")
		}
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.vcs")
			}
			if (project.VCS.current != project.VCS.git) {
				throw new IllegalStateException("Not git: " + project.VCS.current)
			}
			tasks.register("calculateBuildConfigVCSRevisionInfo", ${CalculateVCSRevisionInfoTask::class.java.name})
		""".trimIndent()

		val first = gradle.run(script, "calculateBuildConfigVCSRevisionInfo").build()
		first.assertSuccess(":calculateBuildConfigVCSRevisionInfo")

		git(gradle.root) {
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Second commit")
		}

		val second = gradle.run(null, "calculateBuildConfigVCSRevisionInfo").build()
		second.assertSuccess(":calculateBuildConfigVCSRevisionInfo")
	}

	@Test fun `will stay up to date when svn didn't change`() {
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
			if (project.VCS.current != project.VCS.svn) {
				throw new IllegalStateException("Not svn: " + project.VCS.current)
			}
			tasks.register("calculateBuildConfigVCSRevisionInfo", ${CalculateVCSRevisionInfoTask::class.java.name})
		""".trimIndent()

		val first = gradle.run(script, "calculateBuildConfigVCSRevisionInfo").build()
		first.assertSuccess(":calculateBuildConfigVCSRevisionInfo")

		val second = gradle.run(null, "calculateBuildConfigVCSRevisionInfo").build()
		second.assertUpToDate(":calculateBuildConfigVCSRevisionInfo")
	}

	@Test fun `will not stay up to date when svn changed`() {
		svn {
			val repoUrl = doCreateRepository(gradle.root.resolve(".repo"))
			doCheckout(repoUrl, gradle.root)
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "First commit")
		}
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.vcs")
			}
			if (project.VCS.current != project.VCS.svn) {
				throw new IllegalStateException("Not svn: " + project.VCS.current)
			}
			tasks.register("calculateBuildConfigVCSRevisionInfo", ${CalculateVCSRevisionInfoTask::class.java.name})
		""".trimIndent()

		val first = gradle.run(script, "calculateBuildConfigVCSRevisionInfo").build()
		first.assertSuccess(":calculateBuildConfigVCSRevisionInfo")

		svn {
			doCommitSingleFile(gradle.root.createTestFileToCommit(), "Second commit")
		}

		val second = gradle.run(null, "calculateBuildConfigVCSRevisionInfo").build()
		second.assertSuccess(":calculateBuildConfigVCSRevisionInfo")
	}
}
