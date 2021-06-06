package net.twisterrob.gradle.quality.development

import net.twisterrob.gradle.quality.tasks.HtmlReportTask
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assumptions.assumeTrue
//import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.Desktop
import kotlin.test.assertEquals

@Suppress("unused") // add @Test to run any fun
@ExtendWith(GradleRunnerRuleExtension::class)
class DevelopmentTest {

	private lateinit var gradle: GradleRunnerRule
	private val resources = DevelopmentTestResources()

	fun `manual test for HTML output`() {
		gradle.basedOn("android-root_app")
		val lintResultsXml = resources.customLint.xml
			.replace("""P:\projects\test-project""", gradle.runner.projectDir.absolutePath)
		gradle.file(lintResultsXml, "build", "reports", "lint-results.xml")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		val violationsHtml = gradle.runner.projectDir.resolve("build/reports/violations.html")
		assertThat(violationsHtml, anExistingFile())

		assumeTrue(Desktop.isDesktopSupported())
		Desktop.getDesktop().browse(violationsHtml.toURI())
	}
}
