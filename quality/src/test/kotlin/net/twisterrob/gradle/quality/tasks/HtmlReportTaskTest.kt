package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.ThrowingSupplier
import java.io.File
import java.time.Duration.ofMinutes
import kotlin.test.assertEquals

/**
 * @see HtmlReportTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class HtmlReportTaskTest {

	private lateinit var gradle: GradleRunnerRule

	@Test fun `runs on empty project`() {
		gradle.basedOn("android-root_app")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		assertThat(gradle.violationsReport("xsl"), anExistingFile())
		assertThat(gradle.violationsReport("xml"), anExistingFile())
		assertThat(gradle.violationsReport("html"), anExistingFile())
	}

	@Test fun `able to relocate outputs`() {
		gradle.basedOn("android-root_app")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name}) {
			    xml.set(project.file("my_report/xmldir/xmlname.xmlext"))
			    html.set(project.file("my_report/htmldir/htmlname.htmlext"))
			    xsl.set(project.file("my_report/xsldir/xslname.xslext"))
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		assertThat(gradle.projectFile("my_report/xmldir/xmlname.xmlext"), anExistingFile())
		assertThat(gradle.projectFile("my_report/htmldir/htmlname.htmlext"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xsldir/xslname.xslext"), anExistingFile())
	}

	@Test fun `relocating the XML moves XSL too`() {
		gradle.basedOn("android-root_app")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name}) {
			    xml.set(project.file("my_report/xmldir/xmlname.xmlext"))
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		assertThat(gradle.violationsReport("html"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xmldir/xmlname.xmlext"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xmldir/violations.xsl"), anExistingFile())
	}

	@Test fun `specifying custom XSL template and output works correctly`() {
		gradle.basedOn("android-root_app")
		gradle.file(SIMPLE_XSL, "src", "input.xsl")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name}) {
			    xml.set(project.file("my_report/xmldir/xmlname.xmlext"))
			    xsl.set(project.file("my_report/xsldir/xslname.xslext"))
				xslTemplate.set(project.file("src/input.xsl"))
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		assertThat(gradle.violationsReport("html"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xmldir/xmlname.xmlext"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xsldir/xslname.xslext"), anExistingFile())
		assertEquals(SIMPLE_XSL, gradle.projectFile("my_report/xsldir/xslname.xslext").readText())
		assertEquals(
			"${System.lineSeparator()}\t\tp=${gradle.projectFile(".").parentFile.name}," +
					"${System.lineSeparator()}\t\tc=0",
			gradle.violationsReport("html").readText()
		)
	}

	@Test fun `specifying custom template works correctly`() {
		gradle.basedOn("android-root_app")
		gradle.file(SIMPLE_XSL, "src", "input.xsl")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name}) {
			    xml.set(project.file("my_report/xmldir/xmlname.xmlext"))
				xslTemplate.set(project.file("src/input.xsl"))
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		assertThat(gradle.violationsReport("html"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xmldir/xmlname.xmlext"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xmldir/input.xsl"), anExistingFile())
		assertEquals(SIMPLE_XSL, gradle.projectFile("my_report/xmldir/input.xsl").readText())
		assertEquals(
			"${System.lineSeparator()}\t\tp=${gradle.projectFile(".").parentFile.name}," +
					"${System.lineSeparator()}\t\tc=0",
			gradle.violationsReport("html").readText()
		)
	}

	@Test fun `runs on lints`() {
		val checks = listOf(
			"Autofill",
			"IconMissingDensityFolder",
			"UnusedIds",
			"UnusedResources"
		)
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
				check = [
					${checks.joinToString(separator = ",\n\t\t\t\t\t") { "'$it'" }}
				]
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "lint", "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
		assertThat(gradle.violationsReport("xsl"), anExistingFile())
		assertThat(gradle.violationsReport("xml"), anExistingFile())
		assertThat(gradle.violationsReport("html"), anExistingFile())
	}

	@Test fun `task is up-to-date when lint results are unchanged`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedIds")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})

			android.lintOptions {
				//noinspection GroovyAssignabilityCheck
				check = ['UnusedIds', 'UnusedResources']
			}
		""".trimIndent()
		gradle.run(script, "lint", "htmlReport").build()

		val result = gradle.runBuild {
			run(null, "htmlReport")
		}

		assertEquals(TaskOutcome.UP_TO_DATE, result.task(":htmlReport")!!.outcome)
	}

	@Test fun `task is re-executed when lint results are changed`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name})

			android.lintOptions {
				//noinspection GroovyAssignabilityCheck
				check = ['IconMissingDensityFolder', 'UnusedResources']
			}
		""".trimIndent()
		gradle.run(script, "lint", "htmlReport").build()
		gradle.basedOn("lint-IconMissingDensityFolder")

		val result = gradle.runBuild {
			run(null, "lint", "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
	}

	@Test fun `task can be cleaned to force re-run`() {
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
		gradle.run(script, "lint", "htmlReport").build()

		val result = gradle.runBuild {
			run(null, "cleanHtmlReport", "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":cleanHtmlReport")!!.outcome)
		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
	}

	@Test fun `clean task removes output`() {
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
		gradle.run(script, "lint", "htmlReport").build()

		val result = gradle.runBuild {
			run(null, "cleanHtmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":cleanHtmlReport")!!.outcome)
		assertThat(gradle.violationsReport("xml"), not(anExistingFile()))
		assertThat(gradle.violationsReport("xslt"), not(anExistingFile()))
		assertThat(gradle.violationsReport("html"), not(anExistingFile()))
	}

	@Test fun `task is capable of handling huge number of violations`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			apply plugin: 'org.gradle.reporting-base'
			task('htmlReport', type: ${HtmlReportTask::class.java.name}) {
				doFirst {
					def xml = lint.lintOptions.xmlOutput
					xml.text = '<issues format="4" by="${HtmlReportTaskTest::class}">'
					xml.withWriterAppend { writer ->
						// Note: I tried to estimate the number of violations to create by measuring free memory:
						// (rt.freeMemory() + (rt.maxMemory() - rt.totalMemory())) / 1024 / 1024 * 30
						// After many tries and empirical measurements, it was still flaky on each execution on GitHub.
						// Since then I bumped Xmx from 128 to 256 and the count from 1500 to 3000.
						// This should be stable and catch any regressions, if the processing goes non-linear.
						3000.times {
							writer.write(
								'<issue id="MyLint" category="Performance" severity="Warning"' +
								'       message="Fake lint" summary="Fake lint" explanation="Fake lint&#10;"' +
								'       priority="3" errorLine1="foo" errorLine2="bar">' +
								'	<location file="does not matter" line="' + it + '" column="0"/>' +
								'</issue>'
							)
						}
					}
					xml.append('</issues>')
				}
			}

			android.lintOptions {
				//noinspection GroovyAssignabilityCheck
				check = ['UnusedResources']
				xmlOutput = project.file("build/report.xml")
			}
		""".trimIndent()
		gradle.runner.projectDir.resolve("gradle.properties")
			// useful for manually checking memory usage: -XX:+HeapDumpOnOutOfMemoryError
			.appendText("org.gradle.jvmargs=-Xmx256M\n")

		val result = assertTimeoutPreemptively(ofMinutes(2), ThrowingSupplier {
			gradle.runBuild {
				run(script, "lint", "htmlReport")
			}
		})

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
	}

	companion object {
		@Language("xsl")
		private val SIMPLE_XSL: String =
			"""
				<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
					<xsl:output method="text" />
					<xsl:template match="/violations">
						p=<xsl:value-of select="@project" />,
						c=<xsl:value-of select="count(.//violation)" />
					</xsl:template>
				</xsl:stylesheet>
			""".trimIndent()
	}
}

private fun GradleRunnerRule.projectFile(relative: String): File =
	this.runner.projectDir.resolve(relative)

private fun GradleRunnerRule.violationsReport(extension: String): File =
	this.projectFile("build/reports/violations.${extension}")
