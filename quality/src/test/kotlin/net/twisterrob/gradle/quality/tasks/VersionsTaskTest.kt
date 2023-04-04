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

	@Test fun `print checkstyle version (Gradle 7 latest)`() {
		gradle.gradleVersion = GradleVersion.version("7.6.1")

		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.checkstyle")
			}
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 7.6.1""")
		result.assertHasOutputLine("""Checkstyle version: 8.45.1""")
	}

	@Test fun `print pmd version (Gradle 7 latest)`() {
		gradle.gradleVersion = GradleVersion.version("7.6.1")

		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.pmd")
			}
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 7.6.1""")
		result.assertHasOutputLine("""PMD version: 6.48.0""")
	}

	@Test fun `print checkstyle version (Gradle 8 latest)`() {
		gradle.gradleVersion = GradleVersion.version("8.0.2")

		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.checkstyle")
			}
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 8.0.2""")
		result.assertHasOutputLine("""Checkstyle version: 8.45.1""")
	}

	@Test fun `print pmd version (Gradle 8 latest)`() {
		gradle.gradleVersion = GradleVersion.version("8.0.2")

		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.pmd")
			}
			tasks.register('qualityVersions', ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":qualityVersions")!!.outcome)
		result.assertHasOutputLine("""Gradle version: 8.0.2""")
		result.assertHasOutputLine("""PMD version: 6.48.0""")
	}

	@Test fun `print checkstyle version (specific version)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.checkstyle")
			}
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
			plugins {
				id("org.gradle.pmd")
			}
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
