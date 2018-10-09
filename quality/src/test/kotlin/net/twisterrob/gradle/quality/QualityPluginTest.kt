package net.twisterrob.gradle.quality

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.assertHasOutputLine
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class QualityPluginTest {

	@Rule @JvmField val gradle = GradleRunnerRule()

	@Test fun `apply report violationReportConsole only on root project`() {
		`given`@
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.quality'
			}
			tasks.named('violationReportConsole').configure { println("Configuring " + it) }
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
			.basedOn("android-all_kinds")
			.run(script, "violationReportConsole")
			.build()

		`then`@
		assertEquals(SUCCESS, result.task(":violationReportConsole")!!.outcome)
		val allOtherTasks = result.tasks.map { it.path } - ":violationReportConsole"
		assertThat(allOtherTasks, not(hasItems(containsString("violationReportConsole"))))
		result.assertHasOutputLine(
			"should be configurable without afterEvaluate",
			"Configuring task ':violationReportConsole'"
		)
	}

	@Test fun `apply report violationReportHtml only on root project`() {
		`given`@
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.quality'
			}
			tasks.named('violationReportHtml').configure { println("Configuring " + it) }
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
			.basedOn("android-all_kinds")
			.run(script, "violationReportHtml")
			.build()

		`then`@
		assertEquals(SUCCESS, result.task(":violationReportHtml")!!.outcome)
		val allOtherTasks = result.tasks.map { it.path } - ":violationReportHtml"
		assertThat(allOtherTasks, not(hasItems(containsString("violationReportHtml"))))
		result.assertHasOutputLine(
			"should be configurable without afterEvaluate",
			"Configuring task ':violationReportHtml'"
		)
	}
}
