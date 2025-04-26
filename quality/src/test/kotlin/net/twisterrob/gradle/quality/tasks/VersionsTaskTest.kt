package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.runBuild
import org.gradle.util.GradleVersion
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see VersionsTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class VersionsTaskTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `print missing versions`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("qualityVersions", ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		result.assertSuccess(":qualityVersions")
		result.assertHasOutputLine(Regex("""Gradle version: .+"""))
		result.assertHasOutputLine("""Checkstyle version: 'checkstyle' plugin not applied""")
		result.assertHasOutputLine("""PMD version: 'pmd' plugin not applied""")
	}

	@Test fun `task is never up to date`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("qualityVersions", ${VersionsTask::class.java.name})
		""".trimIndent()
		gradle.file(script, gradle.buildFile.name)

		gradle.run(null, "qualityVersions").build().assertSuccess(":qualityVersions")
		gradle.run(null, "qualityVersions").build().assertSuccess(":qualityVersions")
		gradle.run(null, "qualityVersions").build().assertSuccess(":qualityVersions")
	}

	@Test fun `print versions when plugins applied late`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.create("qualityVersions", ${VersionsTask::class.java.name}) // Intentionally eagerly creating.
			afterEvaluate {
				apply plugin: "org.gradle.checkstyle"
				apply plugin: "org.gradle.pmd"
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		result.assertSuccess(":qualityVersions")
		result.assertHasOutputLine(Regex("""Gradle version: .+"""))
		result.assertHasOutputLine(Regex("""Checkstyle version: \d+(\.\d+)+"""))
		result.assertHasOutputLine(Regex("""PMD version: \d+(\.\d+)+"""))
	}

	@Test fun `print checkstyle version (Gradle 8 latest)`() {
		gradle.gradleVersion = GradleVersion.version("8.14")

		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.checkstyle")
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("qualityVersions", ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		result.assertSuccess(":qualityVersions")
		result.assertHasOutputLine("""Gradle version: 8.14""")
		result.assertHasOutputLine("""Checkstyle version: 9.3""")
	}

	@Test fun `print pmd version (Gradle 8 latest)`() {
		gradle.gradleVersion = GradleVersion.version("8.14")

		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.pmd")
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("qualityVersions", ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		result.assertSuccess(":qualityVersions")
		result.assertHasOutputLine("""Gradle version: 8.14""")
		result.assertHasOutputLine("""PMD version: 6.55.0""")
	}

	@Test fun `print checkstyle version (specific version)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.checkstyle")
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			checkstyle {
				toolVersion = '6.0'
			}
			tasks.register("qualityVersions", ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		result.assertSuccess(":qualityVersions")
		result.assertHasOutputLine("""Checkstyle version: 6.0""")
	}

	@Test fun `print pmd version (specific version)`() {
		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.pmd")
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			pmd {
				toolVersion = '5.0.0'
			}
			tasks.register("qualityVersions", ${VersionsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "qualityVersions")
		}

		result.assertSuccess(":qualityVersions")
		result.assertHasOutputLine("""PMD version: 5.0.0""")
	}
}
