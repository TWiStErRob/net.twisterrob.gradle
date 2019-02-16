package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VersionsTaskTest {

	@Rule @JvmField val gradle = GradleRunnerRule()

	@Test fun `print missing versions`() {
		@Language("gradle")
		val script = """
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: .+""".toRegex())
		result.assertHasOutputLine("""Checkstyle version: 'checkstyle' plugin not applied""")
		result.assertHasOutputLine("""PMD version: 'pmd' plugin not applied""")
		result.assertHasOutputLine("""FindBugs version: 'findbugs' plugin not applied""")
	}

	@Test fun `print checkstyle version`() {
		gradle.setGradleVersion("4.2.1")

		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 4.2.1""")
		result.assertHasOutputLine("""Checkstyle version: 6.19""")
	}

	@Test fun `print pmd version`() {
		gradle.setGradleVersion("4.2.1")

		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 4.2.1""")
		result.assertHasOutputLine("""PMD version: 5.6.1""")
	}

	@Test fun `print findbugs version`() {
		gradle.setGradleVersion("4.2.1")

		@Language("gradle")
		val script = """
			apply plugin: 'findbugs'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 4.2.1""")
		result.assertHasOutputLine("""FindBugs version: 3.0.1""")
	}
}
