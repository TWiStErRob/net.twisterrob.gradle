package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/**
 * @see VersionsTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class VersionsTaskTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `print missing versions`() {
		@Language("gradle")
		val script = """
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine(Regex("""Gradle version: .+"""))
		result.assertHasOutputLine("""Checkstyle version: 'checkstyle' plugin not applied""")
		result.assertHasOutputLine("""PMD version: 'pmd' plugin not applied""")
	}

	@Test fun `print checkstyle version (Gradle 5 earliest)`() {
		gradle.gradleVersion = GradleVersion.version("5.4.1")

		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 5.4.1""")
		result.assertHasOutputLine("""Checkstyle version: 8.17""")
	}

	@Test fun `print pmd version (Gradle 5 earliest)`() {
		gradle.gradleVersion = GradleVersion.version("5.4.1")

		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 5.4.1""")
		result.assertHasOutputLine("""PMD version: 6.8.0""")
	}

	@Test fun `print checkstyle version (Gradle 5 latest)`() {
		gradle.gradleVersion = GradleVersion.version("5.6.4")

		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 5.6.4""")
		result.assertHasOutputLine("""Checkstyle version: 8.17""")
	}

	@Test fun `print pmd version (Gradle 5 latest)`() {
		gradle.gradleVersion = GradleVersion.version("5.6.4")

		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 5.6.4""")
		result.assertHasOutputLine("""PMD version: 6.15.0""")
	}

	@Test fun `print checkstyle version (Gradle 6 latest)`() {
		gradle.gradleVersion = GradleVersion.version("6.9.2")

		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 6.9.2""")
		result.assertHasOutputLine("""Checkstyle version: 8.37""")
	}

	@Test fun `print pmd version (Gradle 6 latest)`() {
		gradle.gradleVersion = GradleVersion.version("6.9.2")

		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 6.9.2""")
		result.assertHasOutputLine("""PMD version: 6.26.0""")
	}

	@Test fun `print checkstyle version (Gradle 7 latest)`() {
		gradle.gradleVersion = GradleVersion.version("7.5.1")

		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 7.5.1""")
		result.assertHasOutputLine("""Checkstyle version: 8.45.1""")
	}

	@Test fun `print pmd version (Gradle 7 latest)`() {
		gradle.gradleVersion = GradleVersion.version("7.5.1")

		@Language("gradle")
		val script = """
			apply plugin: 'pmd'
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 7.5.1""")
		result.assertHasOutputLine("""PMD version: 6.39.0""")
	}

	@Test fun `print checkstyle version (specific version)`() {
		@Language("gradle")
		val script = """
			apply plugin: 'checkstyle'
			checkstyle {
				toolVersion = '6.0'
			}
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
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
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""PMD version: 5.0.0""")
	}
}
