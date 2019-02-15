package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * @see HtmlReportTask
 */
// TODO aapt2 use "lint" task instead of "lintDebug" in these tests
class HtmlReportTaskTest {

	@Rule @JvmField val gradle = GradleRunnerRule(false)

	@Test fun `runs on empty project`() {
		`given`@
		gradle.basedOn("android-root_app")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
			.run(script, "htmlReport")
			.build()

		`then`@
		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		assertThat(gradle.violationsReport("xsl"), anExistingFile())
		assertThat(gradle.violationsReport("xml"), anExistingFile())
		assertThat(gradle.violationsReport("html"), anExistingFile())
	}

	@Test fun `runs on lints`() {
		val checks = listOf(
			"Autofill",
			"IconMissingDensityFolder",
			"IconXmlAndPng",
			"UnusedResources"
		)
		`given`@
		gradle.basedOn("android-root_app")
		checks.forEach { check -> gradle.basedOn("lint-$check") }
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})

			android.defaultConfig.targetSdkVersion 28 // to trigger Autofill
			android.lintOptions {
				abortOnError = false
				//noinspection GroovyAssignabilityCheck
				check = [${checks.joinToString(prefix = "\n\t\t", separator = ",\n\t\t", postfix = "\n\t") { "'$it'" }}]
			}
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
			.run(script, "lintDebug", "htmlReport")
			.build()

		`then`@
		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		assertThat(gradle.violationsReport("xsl"), anExistingFile())
		assertThat(gradle.violationsReport("xml"), anExistingFile())
		assertThat(gradle.violationsReport("html"), anExistingFile())
	}

	@Test fun `task is up-to-date when lint results are unchanged`() {
		`given`@
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-IconXmlAndPng")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})

			android.lintOptions {
				//noinspection GroovyAssignabilityCheck
				check = ['IconXmlAndPng','UnusedResources']
			}
		""".trimIndent()
		gradle.run(script, "lintDebug", "htmlReport").build()

		val result: BuildResult
		`when`@
		result = gradle
			.run(null, "htmlReport")
			.build()

		assertEquals(TaskOutcome.UP_TO_DATE, result.task(":htmlReport")!!.outcome)
	}

	@Test fun `task is re-executed when lint results are changed`() {
		`given`@
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})

			android.lintOptions {
				//noinspection GroovyAssignabilityCheck
				check = ['IconMissingDensityFolder','UnusedResources']
			}
		""".trimIndent()
		gradle.run(script, "lintDebug", "htmlReport").build()
		gradle.basedOn("lint-IconMissingDensityFolder")

		val result: BuildResult
		`when`@
		result = gradle
			.run(null, "lintDebug", "htmlReport")
			.build()

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
	}

	@Test fun `task can be cleaned to force re-run`() {
		`given`@
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})

			android.lintOptions {
				//noinspection GroovyAssignabilityCheck
				check = ['UnusedResources']
			}
		""".trimIndent()
		gradle.run(script, "lintDebug", "htmlReport").build()

		val result: BuildResult
		`when`@
		result = gradle
			.run(null, "cleanHtmlReport", "htmlReport")
			.build()

		assertEquals(TaskOutcome.SUCCESS, result.task(":cleanHtmlReport")!!.outcome)
		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
	}

	@Test fun `clean task removes output`() {
		`given`@
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})

			android.lintOptions {
				//noinspection GroovyAssignabilityCheck
				check = ['UnusedResources']
			}
		""".trimIndent()
		gradle.run(script, "lintDebug", "htmlReport").build()

		val result: BuildResult
		`when`@
		result = gradle
			.run(null, "cleanHtmlReport")
			.build()

		assertEquals(TaskOutcome.SUCCESS, result.task(":cleanHtmlReport")!!.outcome)
		assertThat(gradle.violationsReport("xml"), not(anExistingFile()))
		assertThat(gradle.violationsReport("xslt"), not(anExistingFile()))
		assertThat(gradle.violationsReport("html"), not(anExistingFile()))
	}
}

private fun GradleRunnerRule.violationsReport(extension: String) =
	this.runner.projectDir.resolve("build/reports/violations.${extension}")
