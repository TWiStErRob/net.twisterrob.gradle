package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.checkstyle.test.CheckstyleTestResources
import net.twisterrob.gradle.pmd.test.PmdTestResources
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.projectFile
import net.twisterrob.gradle.test.runBuild
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see FileCountReportTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class FileCountReportTaskIntgTest : BaseIntgTest() {

	companion object {
		private val CONFIG_PATH_CS: Array<String> = arrayOf("config", "checkstyle", "checkstyle.xml")
		private val CONFIG_PATH_PMD: Array<String> = arrayOf("config", "pmd", "pmd.xml")
		private val SOURCE_PATH: Array<String> = arrayOf("src", "main", "java")
	}

	override lateinit var gradle: GradleRunnerRule

	private val checkstyle = CheckstyleTestResources()
	private val pmd = PmdTestResources { gradle.gradleVersion }

	@Test fun `get total violation counts`() {
		gradle.file(checkstyle.simple.content, *SOURCE_PATH, "Checkstyle.java")
		gradle.file(checkstyle.simple.config, *CONFIG_PATH_CS)
		gradle.file(pmd.simple.content, *SOURCE_PATH, "Pmd.java")
		gradle.file(pmd.simple.config, *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'net.twisterrob.pmd'

			tasks.register('printViolationCount', ${FileCountReportTask::class.java.name})
		""".trimIndent()

		gradle.runBuild {
			basedOn("android-root_app")
			run(script, "checkstyleAll", "pmdAll", "printViolationCount")
		}

		val resultFile = gradle.violationsReport("count")
		assertThat(resultFile, anExistingFile())
		assertThat(resultFile.readText(), equalTo("3"))
	}

	@Test fun `get total violation counts into a specific file`() {
		gradle.file(checkstyle.simple.content, *SOURCE_PATH, "Checkstyle.java")
		gradle.file(checkstyle.simple.config, *CONFIG_PATH_CS)
		gradle.file(pmd.simple.content, *SOURCE_PATH, "Pmd.java")
		gradle.file(pmd.simple.config, *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'net.twisterrob.pmd'

			tasks.register('printViolationCount', ${FileCountReportTask::class.java.name}) {
				output.set(file("problems.txt"))
			}
		""".trimIndent()

		gradle.runBuild {
			basedOn("android-root_app")
			run(script, "checkstyleAll", "pmdAll", "printViolationCount")
		}

		val resultFile = gradle.projectFile("problems.txt")
		assertThat(gradle.violationsReport("count"), not(anExistingFile()))
		assertThat(resultFile, anExistingFile())
		assertThat(resultFile.readText(), equalTo("3"))
	}
}
