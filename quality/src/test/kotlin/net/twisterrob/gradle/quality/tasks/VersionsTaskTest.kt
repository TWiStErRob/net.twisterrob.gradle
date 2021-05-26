package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(GradleRunnerRuleExtension::class)
class VersionsTaskTest {

	private lateinit var gradle: GradleRunnerRule

	@Test fun `print missing versions`() {
		@Language("gradle")
		val script = """
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine(Regex("""Gradle version: .+"""))
		result.assertHasOutputLine("""Checkstyle version: 'checkstyle' plugin not applied""")
		result.assertHasOutputLine("""PMD version: 'pmd' plugin not applied""")
	}

 	@Test fun `print checkstyle version (Gradle 4 earliest)`() {
		gradle.setGradleVersion("4.4")

		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 4.4""")
		result.assertHasOutputLine("""Checkstyle version: 6.19""")
	}

	@Test fun `print pmd version (Gradle 4 earliest)`() {
		gradle.setGradleVersion("4.4")

		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 4.4""")
		result.assertHasOutputLine("""PMD version: 5.6.1""")
	}

	@Test fun `print checkstyle version (Gradle 4 latest)`() {
		gradle.setGradleVersion("4.10.3")

		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 4.10.3""")
		result.assertHasOutputLine("""Checkstyle version: 6.19""")
	}

	@Test fun `print pmd version (Gradle 4 latest)`() {
		gradle.setGradleVersion("4.10.3")

		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 4.10.3""")
		result.assertHasOutputLine("""PMD version: 5.6.1""")
	}

	@Test fun `print checkstyle version (Gradle 5 latest)`() {
		gradle.setGradleVersion("5.6.4")

		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 5.6.4""")
		result.assertHasOutputLine("""Checkstyle version: 8.17""")
	}

	@Test fun `print pmd version (Gradle 5 latest)`() {
		gradle.setGradleVersion("5.6.4")

		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 5.6.4""")
		result.assertHasOutputLine("""PMD version: 6.15.0""")
	}

	@Test fun `print checkstyle version (Gradle 6 latest)`() {
		gradle.setGradleVersion("6.9")

		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 6.9""")
		result.assertHasOutputLine("""Checkstyle version: 8.37""")
	}

	@Test fun `print pmd version (Gradle 6 latest)`() {
		gradle.setGradleVersion("6.9")

		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 6.9""")
		result.assertHasOutputLine("""PMD version: 6.26.0""")
	}

	@Test fun `print checkstyle version (specific version)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			checkstyle {
				toolVersion = '6.0'
			}
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Checkstyle version: 6.0""")
	}

	@Test fun `print pmd version (specific version)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			pmd {
				toolVersion = '5.0.0'
			}
			task('qualityVersions', type: ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""PMD version: 5.0.0""")
	}
}
