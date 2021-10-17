package net.twisterrob.gradle.quality.development

import net.twisterrob.gradle.quality.tasks.HtmlReportTask
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import kotlin.test.assertEquals

/**
 * Add [Test] to run any fun.
 */
@Suppress("unused")
@ExtendWith(GradleRunnerRuleExtension::class)
class DevelopmentTest {

	private lateinit var gradle: GradleRunnerRule
	private val resources = DevelopmentTestResources()

	//	@Test
	fun `manual test for running XSL on XML output`() {
		val originalViolationsXml =
			File("P:\\projects\\workspace\\net.twisterrob.gradle\\temp\\examples\\ColorFilters\\build\\reports\\violations.xml")

		val violationsHtml = gradle.runner.projectDir.resolve("violations.html")

		TransformerFactory
			.newInstance()
			.newTransformer(StreamSource(HtmlReportTask::class.java.getResourceAsStream("/violations.xsl")!!.reader()))
			.transform(StreamSource(originalViolationsXml.reader()), StreamResult(violationsHtml))

		openHtml(violationsHtml)
	}

	//	@Test
	fun `manual test for HTML output`() {
		gradle.basedOn("android-root_app")
		val lintResultsXml = resources.customLint.typographyFractions.xmlReport
			.replace("""P:\projects\test-project""", gradle.runner.projectDir.absolutePath)
		gradle.file(lintResultsXml, "build", "reports", "lint-results.xml")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport").withDebug(true)
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		openHtml(gradle.runner.projectDir.resolve("build/reports/violations.html"))
	}

	//	@Test
	fun `manual test for a specific lint failure`() {
		gradle.basedOn("android-root_app")
		val stringsXml = resources.customLint.typographyFractions.violation
		gradle.file(stringsXml, "src", "main", "res", "values", "strings.xml")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			android.lintOptions.checkOnly("TypographyFractions")
			task('htmlReport', type: ${HtmlReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "lint", "htmlReport").withDebug(true)
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		openHtml(gradle.runner.projectDir.resolve("build/reports/violations.html"))
	}
}

private fun openHtml(html: File) {
	assertThat(html, anExistingFile())
	Runtime.getRuntime().exec("""cmd /k "start "" "${html.toURI()}""""")
}
