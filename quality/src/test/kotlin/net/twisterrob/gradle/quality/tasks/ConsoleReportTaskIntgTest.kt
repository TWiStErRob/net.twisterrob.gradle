package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.checkstyle.test.CheckstyleTestResources
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.pmd.test.PmdTestResources
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/**
 * @see ConsoleReportTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class ConsoleReportTaskIntgTest : BaseIntgTest() {

	companion object {
		private val CONFIG_PATH_CS: Array<String> = arrayOf("config", "checkstyle", "checkstyle.xml")
		private val CONFIG_PATH_PMD: Array<String> = arrayOf("config", "pmd", "pmd.xml")
		private val MANIFEST_PATH: Array<String> = arrayOf("src", "main", "AndroidManifest.xml")
		private val SOURCE_PATH: Array<String> = arrayOf("src", "main", "java")

		private val VIOLATION_PATTERN: Regex = Regex("""([A-Z][a-zA-Z0-9_]+?)_(\d)\.java""")
	}

	override lateinit var gradle: GradleRunnerRule

	private val checkstyle = CheckstyleTestResources()
	private val pmd = PmdTestResources()

	@Test fun `get total violation counts on root project`() {
		gradle.file(checkstyle.simple.content, *SOURCE_PATH, "Checkstyle.java")
		gradle.file(checkstyle.simple.config, *CONFIG_PATH_CS)
		gradle.file(pmd.simple.content1, *SOURCE_PATH, "Pmd.java")
		gradle.file(pmd.simple.config, *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.checkstyle'
			apply plugin: 'net.twisterrob.gradle.plugin.pmd'

			tasks.register('printViolationCount', ${ConsoleReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "checkstyleAll", "pmdAll", "printViolationCount")
		}

		// TODO find another CheckStyle violation that's more specific
		result.assertHasOutputLine("Summary\t(total: 3)\t         2\t  1")
	}

	@Test fun `get total violation counts`() {
		gradle.file(checkstyle.simple.content, "module", *SOURCE_PATH, "Cs.java")
		gradle.file(checkstyle.simple.config, *CONFIG_PATH_CS)
		gradle.file(pmd.simple.content1, "module", *SOURCE_PATH, "Pmd.java")
		gradle.file(pmd.simple.config, *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.gradle.plugin.checkstyle'
				apply plugin: 'net.twisterrob.gradle.plugin.pmd'
			}
			tasks.register('printViolationCount', ${ConsoleReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-single_module")
			run(script, "checkstyleAll", "pmdAll", "printViolationCount")
		}

		// TODO find another CheckStyle violation that's more specific
		result.assertHasOutputLine("Summary\t(total: 3)\t         2\t  1")
	}

	@Test fun `get per module violation counts`() {
		checkstyle.multi.contents.forEach { (name, content) ->
			val match = VIOLATION_PATTERN.matchEntire(name) ?: error("$name doesn't match $VIOLATION_PATTERN")
			val checkName = match.groups[1]!!.value
			@Suppress("UNUSED_VARIABLE")
			val checkCount = match.groups[2]!!.value.toInt()
			val checkstyleXmlContents = checkstyle.multi.config.replace("CheckName", checkName)
			gradle.file(checkstyleXmlContents, checkName, *CONFIG_PATH_CS)
			gradle.file("""<manifest package="checkstyle.${checkName}" />""", checkName, *MANIFEST_PATH)
			gradle.file(content, checkName, *SOURCE_PATH, name)
			gradle.settingsFile.appendText("include ':${checkName}'${System.lineSeparator()}")
		}

		@Language("gradle")
		val script = """
			subprojects {
				apply plugin: 'com.android.library'
				apply plugin: 'net.twisterrob.gradle.plugin.checkstyle'
			}
			tasks.register('printViolationCounts', ${ConsoleReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-multi_module")
			run(script, "checkstyleAll", "printViolationCounts")
		}

		assertThat(
			result.output, containsString(
				"""
					module        	variant  	checkstyle
					:EmptyBlock   	*        	         3
					:MemberName   	*        	         2
					:UnusedImports	*        	         4
					Summary       	(total: 9)	         9
				""".trimIndent().replace(Regex("""\r?\n"""), System.lineSeparator())
			)
		)
	}

	@Test fun `task is re-executed when violation results are changed`() {
		gradle.basedOn("android-root_app")
		gradle.file(checkstyle.simple.config, *CONFIG_PATH_CS)
		gradle.file(pmd.simple.config, *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.checkstyle'
			apply plugin: 'net.twisterrob.gradle.plugin.pmd'

			tasks.register('printViolationCount', ${ConsoleReportTask::class.java.name})
		""".trimIndent()

		gradle.file(checkstyle.simple.content, *SOURCE_PATH, "Checkstyle.java")
		gradle.run(script, "checkstyleAll", "pmdAll", "printViolationCount").build()
		gradle.file(pmd.simple.content1, *SOURCE_PATH, "Pmd.java")

		val result = gradle.runBuild {
			run(null, "checkstyleAll", "pmdAll", "printViolationCount")
		}

		assertEquals(SUCCESS, result.task(":printViolationCount")!!.outcome)
	}

	@Test fun `gather lint report when lintOptions-xmlOutput is set`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")

		@Language("gradle")
		val script = """
			tasks.register('printViolationCount', ${ConsoleReportTask::class.java.name})
			android.lintOptions.xmlOutput = new File(buildDir, "reports/my-lint/results.xml")
			android.lintOptions.check = ['UnusedResources']
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "lintDebug", "lintRelease", "printViolationCount")
		}

		assertEquals(SUCCESS, result.task(":printViolationCount")!!.outcome)
		result.assertHasOutputLine("Summary\t(total: 1)\t   1\t          0")
	}

	@Test fun `gather unique lint violations when multiple variants are linted`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")

		@Language("gradle")
		val script = """
			tasks.register('printViolationCount', ${ConsoleReportTask::class.java.name})
			android.lintOptions.check = ['UnusedResources']
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "lint", "lintDebug", "lintRelease", "lintVitalRelease", "printViolationCount")
		}

		assertEquals(SUCCESS, result.task(":printViolationCount")!!.outcome)
		result.assertHasOutputLine("Summary\t(total: 1)\t   1\t          0")
	}

	@Test fun `do not gather non-existent reports`() {
		gradle.basedOn("android-root_app")

		@Language("gradle")
		val script = """
			tasks.register('printViolationCount', ${ConsoleReportTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "printViolationCount", "--info")
		}

		assertEquals(SUCCESS, result.task(":printViolationCount")!!.outcome)
		result.assertNoOutputLine(
			"""
				Some problems were found with the configuration of task ':printViolationCount'\..*
			""".trimIndent().toRegex()
		)
		result.assertNoOutputLine(Regex(""" - File '(.*)' specified for property '.*' does not exist\."""))
		result.assertHasOutputLine("Summary\t(total: 0)")
		when {
			AGPVersions.v71x <= AGPVersions.UNDER_TEST -> {
				result.assertHasOutputLine(
					"""
						Missing report for task ':lintReportDebug'.*: .*\blint-results-debug.xml
					""".trimIndent().toRegex()
				)
				result.assertHasOutputLine(
					"""
						Missing report for task ':lintReportRelease'.*: .*\blint-results-release.xml
					""".trimIndent().toRegex()
				)
			}
			AGPVersions.v70x <= AGPVersions.UNDER_TEST -> {
				result.assertHasOutputLine(Regex("""Missing report for task ':lintDebug'.*: .*\blint-results-debug.xml"""))
				result.assertHasOutputLine(Regex("""Missing report for task ':lintRelease'.*: .*\blint-results-release.xml"""))
			}
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.UNDER_TEST)
		}
	}
}
