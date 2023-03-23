package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.checkstyle.test.CheckstyleTestResources
import net.twisterrob.gradle.pmd.test.PmdTestResources
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertFailed
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see ValidateViolationsTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class ValidateViolationsTaskIntgTest : BaseIntgTest() {

	companion object {
		private val CONFIG_PATH_CS: Array<String> = arrayOf("config", "checkstyle", "checkstyle.xml")
		private val CONFIG_PATH_PMD: Array<String> = arrayOf("config", "pmd", "pmd.xml")
		private val SOURCE_PATH: Array<String> = arrayOf("src", "main", "java")
	}

	override lateinit var gradle: GradleRunnerRule

	private val checkstyle = CheckstyleTestResources()
	private val pmd = PmdTestResources()

	@Test fun `fails when violations`() {
		gradle.file(checkstyle.simple.content, *SOURCE_PATH, "Checkstyle.java")
		gradle.file(checkstyle.simple.config, *CONFIG_PATH_CS)
		gradle.file(pmd.simple.content1, *SOURCE_PATH, "Pmd.java")
		gradle.file(pmd.simple.config, *CONFIG_PATH_PMD)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.checkstyle'
			apply plugin: 'net.twisterrob.gradle.plugin.pmd'

			tasks.register('validateViolations', ${ValidateViolationsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runFailingBuild {
			basedOn("android-root_app")
			run(script, "checkstyleAll", "pmdAll", "validateViolations")
		}

		result.assertFailed(":validateViolations")
	}

	@Test fun `passes when no violations`() {
		@Language("gradle")
		val script = """
			tasks.register('validateViolations', ${ValidateViolationsTask::class.java.name})
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "validateViolations")
		}

		result.assertSuccess(":validateViolations")
	}
}
