package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HtmlReportTaskTest {

	@Rule @JvmField val gradle = GradleRunnerRule(false)

	@Test fun `runs on empty project`() {
		`given`@
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
			.basedOn("android-root_app")
			.run(script, "htmlReport")
			.build()

		`then`@
		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		assertTrue(gradle.runner.projectDir.resolve("build/reports/violations.xsl").exists())
		assertTrue(gradle.runner.projectDir.resolve("build/reports/violations.xml").exists())
		assertTrue(gradle.runner.projectDir.resolve("build/reports/violations.html").exists())
	}
}
