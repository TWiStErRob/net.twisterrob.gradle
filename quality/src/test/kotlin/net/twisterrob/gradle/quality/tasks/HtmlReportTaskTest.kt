package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
			@SuppressWarnings("GroovyUnusedAssignment")
			task('htmlReport', type: ${HtmlReportTask::class.java.name}) {
				doFirst {
					/**
					 * Calculated based on empirical measurements with -Xmx3G.
					 * 500 violations used 19MB
					 * 1000 violations used 33MB
					 * 2000 violations used 67MB
					 * 3000 violations used 100MB
					 * 5000 violations used 166MB
					 * 10000 violations used 333MB
					 * These follow a linear trend and 1MB is capable of holding ~{@value} violations.
					 */ 
					double multiplier = 30
					def xml = lint.lintOptions.xmlOutput
					xml.text = '<issues format="4" by="${HtmlReportTaskTest::class}">'
					xml.withWriterAppend { writer ->
						Runtime runtime = Runtime.getRuntime()
						long potentialFreeB = runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())
						long potentialFreeMB = (long)(potentialFreeB / 1024 / 1024)
						long totalMB = (long)(runtime.totalMemory() / 1024 / 1024)
						long maxMB = (long)(runtime.maxMemory() / 1024 / 1024)
						int count = (potentialFreeMB * multiplier).toInteger()
						println("There's ${'$'}{potentialFreeMB}MB free out of ${'$'}{maxMB}MB (${'$'}{totalMB}MB allocated).")
						println("Generating ${'$'}{count} lint violations.")
						if (count < 1000) {
							throw new IllegalStateException("Bad test setup, ${'$'}{count} is not a 'huge number', probably not enough memory.")
						}
						count.times {
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
			.appendText("org.gradle.jvmargs=-Xmx128M\n")

		val result = gradle.runBuild {
			run(script, "lint", "htmlReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":htmlReport")!!.outcome)
	}
}

private fun GradleRunnerRule.violationsReport(extension: String) =
	this.runner.projectDir.resolve("build/reports/violations.${extension}")
