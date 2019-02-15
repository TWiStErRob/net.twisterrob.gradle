package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * @see HtmlReportTask
 */
class HtmlReportTaskTest {

	@Rule @JvmField val gradle = GradleRunnerRule(false)

	@Test fun `runs on empty project`() {
		`given`@
		gradle
			.basedOn("android-root_app")
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
		gradle.assertViolationsReportExists("xsl")
		gradle.assertViolationsReportExists("xml")
		gradle.assertViolationsReportExists("html")
	}

	@Test fun `runs on lints`() {
		`given`@
		gradle
			.basedOn("android-root_app")
			.basedOn("lint-many_violations")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})

			android.defaultConfig.targetSdkVersion 28 // to trigger Autofill
			android.lintOptions {
				abortOnError = false
				//checkAllWarnings = true
				//noinspection GroovyAssignabilityCheck
				check = [
					'Autofill',
					'IconMissingDensityFolder',
					'IconXmlAndPng',
					'UnusedResources',
				]
			}
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
			// TODO aapt2 use "lint" task
			.run(script, "lintDebug", "htmlReport")
			.build()

		`then`@
		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		gradle.assertViolationsReportExists("xsl")
		gradle.assertViolationsReportExists("xml")
		gradle.assertViolationsReportExists("html")
	}
}

private fun GradleRunnerRule.assertViolationsReportExists(extension: String) {
	assertTrue(this.runner.projectDir.resolve("build/reports/violations.${extension}").exists())
}
