package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.checkstyle.test.csRes
import net.twisterrob.gradle.common.listFilesInDirectory
import net.twisterrob.gradle.pmd.test.pmdRes
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
import java.io.File
import kotlin.test.assertEquals

@ExtendWith(GradleRunnerRuleExtension::class)
class ValidateViolationsTaskTest {

	companion object {
		val CONFIG_PATH_CS = arrayOf("config", "checkstyle", "checkstyle.xml")
		val CONFIG_PATH_PMD = arrayOf("config", "pmd", "pmd.xml")
		val MANIFEST_PATH = arrayOf("src", "main", "AndroidManifest.xml")
		val SOURCE_PATH = arrayOf("src", "main", "java")

		val VIOLATION_PATTERN = Regex("""([A-Z][a-zA-Z0-9_]+?)_(\d).java""")
	}

	private lateinit var gradle: GradleRunnerRule

	@Test fun `get total violation counts on root project`() {
		gradle.file(gradle.csRes.failingContent, *SOURCE_PATH, "Checkstyle.java")
		gradle.file(gradle.csRes.failingConfig, *CONFIG_PATH_CS)
		gradle.file(gradle.pmdRes.failingContent, *SOURCE_PATH, "Pmd.java")
		gradle.file(gradle.pmdRes.failingConfig, *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'net.twisterrob.pmd'

			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "checkstyleAll", "pmdAll", "printViolationCount")
		}

		// TODO find another CheckStyle violation that's more specific
		result.assertHasOutputLine("Summary\t(total: 3)\t         2\t  1")
	}

	@Test fun `get total violation counts`() {
		gradle.file(gradle.csRes.failingContent, "module", *SOURCE_PATH, "Cs.java")
		gradle.file(gradle.csRes.failingConfig, *CONFIG_PATH_CS)
		gradle.file(gradle.pmdRes.failingContent, "module", *SOURCE_PATH, "Pmd.java")
		gradle.file(gradle.pmdRes.failingConfig, *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.checkstyle'
				apply plugin: 'net.twisterrob.pmd'
			}
			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-single_module")
			run(script, "checkstyleAll", "pmdAll", "printViolationCount")
		}

		// TODO find another CheckStyle violation that's more specific
		result.assertHasOutputLine("Summary\t(total: 3)\t         2\t  1")
	}

	@Test fun `get per module violation counts`() {
		val template = gradle.templateFile("checkstyle-multiple_violations/checkstyle-template.xml").readText()
		val dir = gradle.templateFile("checkstyle-multiple_violations")
		dir.listFilesInDirectory().sorted().forEach { file: File ->
			println("Building module from ${file}")
			VIOLATION_PATTERN.matchEntire(file.name)?.apply {
				val checkName = groups[1]!!.value
				@Suppress("UNUSED_VARIABLE")
				val checkCount = groups[2]!!.value.toInt()
				val checkstyleXmlContents = template.replace("\${CheckName}", checkName)
				gradle.file(checkstyleXmlContents, checkName, *CONFIG_PATH_CS)
				gradle.file("""<manifest package="checkstyle.${checkName}" />""", checkName, *MANIFEST_PATH)
				gradle.file(file.readText(), checkName, *SOURCE_PATH, file.name)
				gradle.settingsFile.appendText("include ':${checkName}'${System.lineSeparator()}")
			}
		}

		@Language("gradle")
		val script = """
			subprojects {
				apply plugin: 'com.android.library'
				apply plugin: 'net.twisterrob.checkstyle'
			}
			task('printViolationCounts', type: ${ValidateViolationsTask::class.java.name})
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
		gradle.file(gradle.csRes.failingConfig, *CONFIG_PATH_CS)
		gradle.file(gradle.pmdRes.failingConfig, *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'net.twisterrob.pmd'

			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name})
		""".trimIndent()

		gradle.file(gradle.csRes.failingContent, *SOURCE_PATH, "Checkstyle.java")
		gradle.run(script, "checkstyleAll", "pmdAll", "printViolationCount").build()
		gradle.file(gradle.pmdRes.failingContent, *SOURCE_PATH, "Pmd.java")

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
			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name})
			android.lintOptions.xmlOutput = new File(buildDir, "reports/my-lint/results.xml")
			android.lintOptions.check = ['UnusedResources']
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "lint", "printViolationCount")
		}

		assertEquals(SUCCESS, result.task(":printViolationCount")!!.outcome)
		result.assertHasOutputLine("Summary\t(total: 1)\t   1\t          0")
	}

	@Test fun `gather unique lint violations when multiple variants are linted`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")

		@Language("gradle")
		val script = """
			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name})
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

		@Suppress("UnusedProperty")
		@Language("properties")
		val props = """
			org.gradle.warning.mode=all
			org.gradle.deprecation.trace=true
		""".trimIndent()
		gradle.runner.projectDir.resolve("gradle.properties").appendText(props)

		@Language("gradle")
		val script = """
			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "printViolationCount", "--info")
		}

		assertEquals(SUCCESS, result.task(":printViolationCount")!!.outcome)
		result.assertNoOutputLine(Regex("""Some problems were found with the configuration of task ':printViolationCount'\..*"""))
		result.assertNoOutputLine(Regex(""" - File '(.*)' specified for property '.*' does not exist\."""))
		result.assertHasOutputLine("Summary\t(total: 0)")
		result.assertHasOutputLine(Regex("""Missing report for task ':lint'.*: .*\blint-results.xml"""))
		result.assertHasOutputLine(Regex("""Missing report for task ':lintDebug'.*: .*\blint-results-debug.xml"""))
		result.assertHasOutputLine(Regex("""Missing report for task ':lintRelease'.*: .*\blint-results-release.xml"""))
	}
}
