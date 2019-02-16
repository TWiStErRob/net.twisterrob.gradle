package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class ValidateViolationsTaskTest {

	companion object {
		val CONFIG_PATH_CS = arrayOf("config", "checkstyle", "checkstyle.xml")
		val CONFIG_PATH_PMD = arrayOf("config", "pmd", "pmd.xml")
		val MANIFEST_PATH = arrayOf("src", "main", "AndroidManifest.xml")
		val SOURCE_PATH = arrayOf("src", "main", "java")

		val VIOLATION_PATTERN = Regex("""([A-Z][a-zA-Z0-9_]+?)_(\d).java""")
	}

	@Rule @JvmField val gradle = GradleRunnerRule()

	@Test fun `get total violation counts on root project`() {
		gradle.file(gradle.templateFile("checkstyle-simple_failure.java").readText(), *SOURCE_PATH, "Checkstyle.java")
		gradle.file(gradle.templateFile("checkstyle-simple_failure.xml").readText(), *CONFIG_PATH_CS)
		gradle.file(gradle.templateFile("pmd-simple_failure.java").readText(), *SOURCE_PATH, "Pmd.java")
		gradle.file(gradle.templateFile("pmd-simple_failure.xml").readText(), *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'net.twisterrob.pmd'

			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name}) {
				action = {/*${Grouper.Start::class.java.name}<${Violations::class.java.name}>*/ results ->
					def count = results.list.sum(0) { /*${Violations::class.java.name}*/ result -> result.violations?.size() ?: 0 }
					println "Violations: ${'$'}{count}"
				}
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "checkstyleAll", "pmdAll", "printViolationCount")
		}

		// TODO find another CheckStyle violation that's more specific
		result.assertHasOutputLine("Violations: 3")
	}

	@Test fun `get total violation counts`() {
		gradle.file(gradle.templateFile("checkstyle-simple_failure.java").readText(), "module", *SOURCE_PATH, "Cs.java")
		gradle.file(gradle.templateFile("checkstyle-simple_failure.xml").readText(), *CONFIG_PATH_CS)
		gradle.file(gradle.templateFile("pmd-simple_failure.java").readText(), "module", *SOURCE_PATH, "Pmd.java")
		gradle.file(gradle.templateFile("pmd-simple_failure.xml").readText(), *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.checkstyle'
				apply plugin: 'net.twisterrob.pmd'
			}
			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name}) {
				action = {/*${Grouper.Start::class.java.name}<${Violations::class.java.name}>*/ results ->
					def count = results.list.sum(0) { /*${Violations::class.java.name}*/ result -> result.violations?.size() ?: 0 }
					println "Violations: ${'$'}{count}"
				}
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-single_module")
			run(script, "checkstyleAll", "pmdAll", "printViolationCount")
		}

		// TODO find another CheckStyle violation that's more specific
		result.assertHasOutputLine("Violations: 3")
	}

	@Test fun `get per module violation counts`() {
		val template = gradle.templateFile("checkstyle-multiple_violations/checkstyle-template.xml").readText()
		val dir = gradle.templateFile("checkstyle-multiple_violations")
		dir.listFiles().sorted().forEach { file: File ->
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
			task('printViolationCounts', type: ${ValidateViolationsTask::class.java.name}) {
				action = {${Grouper.Start::class.java.name}<${Violations::class.java.name}> results ->
					results.count().by.parser.by.module.by.variant.group()['checkstyle'].each {module, byVariant ->
						println "\t${'$'}{module}:"
						byVariant.each {variant, resultCount ->
							if (resultCount != null) {
								println "\t\t${'$'}{variant}: ${'$'}{resultCount}"
							}
						}
					}
				}
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-multi_module")
			run(script, "checkstyleAll", "printViolationCounts")
		}

		assertThat(
			result.output, containsString(
				"""
				:printViolationCounts
					:EmptyBlock:
						${ALL_VARIANTS_NAME}: 3
					:MemberName:
						${ALL_VARIANTS_NAME}: 2
					:UnusedImports:
						${ALL_VARIANTS_NAME}: 4
				""".trimIndent().replace(Regex("""\r?\n"""), System.lineSeparator())
			)
		)
	}

	@Test fun `task is re-executed when violation results are changed`() {
		gradle.basedOn("android-root_app")
		gradle.file(gradle.templateFile("checkstyle-simple_failure.xml").readText(), *CONFIG_PATH_CS)
		gradle.file(gradle.templateFile("pmd-simple_failure.xml").readText(), *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'net.twisterrob.pmd'

			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name}) {
				action = {/*${Grouper.Start::class.java.name}<${Violations::class.java.name}>*/ results ->
					def count = results.list.sum(0) { /*${Violations::class.java.name}*/ result -> result.violations?.size() ?: 0 }
					println "Violations: ${'$'}{count}"
				}
			}
		""".trimIndent()

		gradle.file(gradle.templateFile("checkstyle-simple_failure.java").readText(), *SOURCE_PATH, "Checkstyle.java")
		gradle.run(script, "checkstyleAll", "pmdAll", "printViolationCount").build()
		gradle.file(gradle.templateFile("pmd-simple_failure.java").readText(), *SOURCE_PATH, "Pmd.java")

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
			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name}) {
				action = {/*${Grouper.Start::class.java.name}<${Violations::class.java.name}>*/ results ->
					def count = results.list.sum(0) { /*${Violations::class.java.name}*/ result -> result.violations?.size() ?: 0 }
					println "Violations: ${'$'}{count}"
				}
			}
			android.lintOptions.xmlOutput = new File(buildDir, "reports/my-lint/results.xml")
			android.lintOptions.check = ['UnusedResources']
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "lint", "printViolationCount")
		}

		assertEquals(SUCCESS, result.task(":printViolationCount")!!.outcome)
		result.assertHasOutputLine("Violations: 1")
	}

	@Test fun `gather unique lint violations when multiple variants are linted`() {
		gradle.basedOn("android-root_app")
		gradle.basedOn("lint-UnusedResources")

		@Language("gradle")
		val script = """
			task('printViolationCount', type: ${ValidateViolationsTask::class.java.name}) {
				action = {/*${Grouper.Start::class.java.name}<${Violations::class.java.name}>*/ results ->
					def count = results.list.sum(0) { /*${Violations::class.java.name}*/ result -> result.violations?.size() ?: 0 }
					println "Violations: ${'$'}{count}"
				}
			}
			android.lintOptions.check = ['UnusedResources']
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "lint", "lintDebug", "lintRelease", "lintVitalRelease", "printViolationCount")
		}

		assertEquals(SUCCESS, result.task(":printViolationCount")!!.outcome)
		result.assertHasOutputLine("Violations: 1")
	}
}
