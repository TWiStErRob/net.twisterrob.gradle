package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.checkstyle.test.CheckstyleTestResources
import net.twisterrob.gradle.pmd.test.PmdTestResources
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.assertUpToDate
import net.twisterrob.gradle.test.projectFile
import net.twisterrob.gradle.test.root
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.test.testName
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.aFileWithSize
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.ThrowingSupplier
import java.io.File
import java.time.Duration.ofMinutes

/**
 * @see HtmlReportTask
 * @see BaseViolationsTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class HtmlReportTaskTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `runs on empty project`() {
		gradle.basedOn("android-root_app")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		result.assertSuccess(":htmlReport")
		assertThat(gradle.violationsReport("xsl"), anExistingFile())
		assertThat(gradle.violationsReport("xml"), anExistingFile())
		assertThat(gradle.violationsReport("html"), anExistingFile())
	}

	@Test fun `able to relocate outputs`() {
		gradle.basedOn("android-root_app")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name}) {
			    xml.set(project.file("my_report/xmldir/xmlname.xmlext"))
			    html.set(project.file("my_report/htmldir/htmlname.htmlext"))
			    xsl.set(project.file("my_report/xsldir/xslname.xslext"))
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		result.assertSuccess(":htmlReport")
		assertThat(gradle.projectFile("my_report/xmldir/xmlname.xmlext"), anExistingFile())
		assertThat(gradle.projectFile("my_report/htmldir/htmlname.htmlext"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xsldir/xslname.xslext"), anExistingFile())
	}

	@Test fun `relocating the XML moves XSL too`() {
		gradle.basedOn("android-root_app")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name}) {
			    xml.set(project.file("my_report/xmldir/xmlname.xmlext"))
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		result.assertSuccess(":htmlReport")
		assertThat(gradle.violationsReport("html"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xmldir/xmlname.xmlext"), anExistingFile())
		assertThat(gradle.projectFile("my_report/xmldir/violations.xsl"), anExistingFile())
	}

	@Test fun `specifying custom XSL template and output works correctly`() {
		gradle.basedOn("android-root_app")
		gradle.file(SIMPLE_XSL, "src", "input.xsl")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name}) {
			    xml.set(project.file("my_report/xmldir/xmlname.xmlext"))
			    xsl.set(project.file("my_report/xsldir/xslname.xslext"))
				xslTemplate.set(project.file("src/input.xsl"))
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		result.assertSuccess(":htmlReport")
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
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name}) {
			    xml.set(project.file("my_report/xmldir/xmlname.xmlext"))
				xslTemplate.set(project.file("src/input.xsl"))
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		result.assertSuccess(":htmlReport")
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
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name})

			android.defaultConfig.targetSdk = 28 // to trigger Autofill
			android.lint {
				abortOnError = false
				${checks.joinToString(separator = "\n\t\t\t\t") { """checkOnly.add("$it")""" }}
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "lint", "htmlReport")
		}

		result.assertSuccess(":htmlReport")
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
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name})

			android.lint {
				checkOnly.add("UnusedIds")
				checkOnly.add("UnusedResources")
			}
		""".trimIndent()
		gradle.run(script, "lint", "htmlReport").build()

		val result = gradle.runBuild {
			run(null, "htmlReport")
		}

		result.assertUpToDate(":htmlReport")
	}

	@Test fun `task is re-executed when lint results are changed`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name})

			android.lint {
				checkOnly.add("IconMissingDensityFolder")
				checkOnly.add("UnusedResources")
			}
		""".trimIndent()
		gradle.run(script, "lint", "htmlReport").build()
		gradle.basedOn("lint-IconMissingDensityFolder")

		val result = gradle.runBuild {
			run(null, "lint", "htmlReport")
		}

		result.assertSuccess(":htmlReport")
	}

	@Test fun `task can be cleaned to force re-run`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name})

			android.lint {
				checkOnly.add("UnusedResources")
			}
		""".trimIndent()
		gradle.run(script, "lint", "htmlReport").build()

		val result = gradle.runBuild {
			run(null, "cleanHtmlReport", "htmlReport")
		}

		result.assertSuccess(":cleanHtmlReport")
		result.assertSuccess(":htmlReport")
	}

	@Test fun `clean task removes output`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name})

			android.lint {
				checkOnly.add("UnusedResources")
			}
		""".trimIndent()
		gradle.run(script, "lint", "htmlReport").build()

		val result = gradle.runBuild {
			run(null, "cleanHtmlReport")
		}

		result.assertSuccess(":cleanHtmlReport")
		assertThat(gradle.violationsReport("xml"), not(anExistingFile()))
		assertThat(gradle.violationsReport("xslt"), not(anExistingFile()))
		assertThat(gradle.violationsReport("html"), not(anExistingFile()))
	}

	@Test fun `task is capable of handling huge number of violations`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality") apply false
			}
			dumpMemory("starting build")
			File xml = project.file("build/reports/lint-results-debug.xml")
			def generate = tasks.register("generateBigReport") {
				outputs.file(xml)
				doFirst {
					dumpMemory("starting generation")
					xml.text = '<issues format="4" by="${HtmlReportTaskTest::class}">'
					xml.withWriterAppend { writer ->
						// Note: I tried to estimate the number of violations to create by measuring free memory:
						// (rt.freeMemory() + (rt.maxMemory() - rt.totalMemory())) / 1024 / 1024 * 30
						// After many tries and empirical measurements, it was still flaky on each execution on GitHub.
						// Since then I bumped Xmx from 128 to 256 and the count from 1500 to 3000.
						// This should be stable and catch any regressions, if the processing goes non-linear.
						2500.times {
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
					dumpMemory("finished generation")
				}
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name}) {
				dependsOn(generate)
				doFirst { dumpMemory("starting report") }
				doLast { dumpMemory("finished report") }
			}
			afterEvaluate { dumpMemory("executing build") }
			
			static void dumpMemory(String message) {
				def rt = Runtime.getRuntime()
				3.times { rt.gc() }
				println(message + ": free=" + rt.freeMemory() + " max=" + rt.maxMemory() + " total=" + rt.totalMemory())
			}
		""".trimIndent()
		gradle.propertiesFile
			// useful for manually checking memory usage: -XX:+HeapDumpOnOutOfMemoryError
			.appendText("org.gradle.jvmargs=-Xmx256M\n")

		val result = assertTimeoutPreemptively(ofMinutes(2), ThrowingSupplier {
			gradle.runBuild {
				run(script, "generateBigReport", "htmlReport")
			}
		})

		result.assertSuccess(":htmlReport")
		//val violationsReport = gradle.violationsReport("html").readText()
		//assertEquals(count, """<div class="violation"""".toRegex().findAll(violationsReport).count())
	}

	@Test fun `runs on multiple reports`(test: TestInfo) {
		val violations = ViolationTestResources(gradle.root)
		val checkstyle = CheckstyleTestResources()
		val pmd = PmdTestResources()
		gradle.basedOn("android-root_app")
		listOf(
			"Autofill",
			"IconMissingDensityFolder",
			"UnusedIds",
			"UnusedResources"
		).forEach { check -> gradle.basedOn("lint-$check") }

		checkstyle.multi.contents.forEach { (name, content) ->
			gradle.file(content, "src", "main", "java", name)
		}

		gradle.file(pmd.simple.content1, "src", "main", "java", "WithoutPackage.java")
		gradle.file(pmd.simple.content2, "src", "main", "java", "pmd", "PrintStack.java")

		gradle.file(violations.everything.lintReport, "build", "reports", "lint-results-debug.xml")
		gradle.file(violations.everything.checkstyleReport, "build", "reports", "checkstyle.xml")
		gradle.file(violations.everything.pmdReport, "build", "reports", "pmd.xml")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.checkstyle")
				id("net.twisterrob.gradle.plugin.pmd")
			}
			tasks.register("htmlReport", ${HtmlReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "htmlReport")
		}

		result.assertSuccess(":htmlReport")
		assertThat(gradle.violationsReport("xsl"), anExistingFile())
		assertThat(gradle.violationsReport("xml"), anExistingFile())
		assertThat(gradle.violationsReport("html"), anExistingFile())
		exposeViolationsInReport(test, violations.everything)
		assertThat(gradle.violationsReport("xsl"), aFileWithSize(greaterThan(0)))
		// If this fails, see net.twisterrob.gradle.quality.tasks.ViolationTestResources.Everything.
		assertEquals(
			violations.everything.violationsXml,
			gradle.violationsReport("xml").readText(),
			gradle.violationsReport("xml").absolutePath
		)
		// If this fails, see net.twisterrob.gradle.quality.tasks.ViolationTestResources.Everything.
		assertEquals(
			violations.everything.violationsHtml,
			gradle.violationsReport("html").readText(),
			gradle.violationsReport("html").absolutePath
		)
	}

	private fun exposeViolationsInReport(test: TestInfo, resources: ViolationTestResources.Everything) {
		val baseDir = File("build/reports/tests/test/outputs").resolve(test.testName)
		listOf(
			gradle.violationsReport("xsl"),
			gradle.violationsReport("xml"),
			gradle.violationsReport("html"),
		).forEach { file ->
			file.copyTo(baseDir.resolve(file.name), overwrite = true)
		}
		baseDir.resolve("violations-expected.xml").writeText(resources.violationsXml)
		baseDir.resolve("violations-expected.html").writeText(resources.violationsHtml)
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
