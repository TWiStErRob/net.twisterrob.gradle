package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.failReason
import net.twisterrob.gradle.test.runFailingBuild
import org.gradle.api.plugins.quality.Pmd
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(GradleRunnerRuleExtension::class)
class PmdTaskTest_ConfigLocation {

	companion object {

		val CONFIG_PATH = arrayOf("config", "pmd", "pmd.xml")
	}

	private lateinit var gradle: GradleRunnerRule

	private lateinit var noChecksConfig: String
	private lateinit var failingConfig: String
	private lateinit var failingContent: String

	@BeforeEach fun setUp() {
		noChecksConfig = gradle.templateFile("pmd-empty.xml").readText()
		failingConfig = gradle.templateFile("pmd-simple_failure.xml").readText()
		failingContent = gradle.templateFile("pmd-simple_failure.java").readText()
	}

	@Test fun `uses rootProject pmd config as a fallback`() {
		gradle.file(failingConfig, *CONFIG_PATH)
		@Suppress("ConstantConditionIf") // do not set up, we want it to use rootProject's
		if (false) {
			gradle.file(noChecksConfig, "module", * CONFIG_PATH)
		}

		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test fun `uses local module pmd config if available`() {
		@Suppress("ConstantConditionIf") // do not set up rootProject's, we want to see if works without as well
		if (false) {
			gradle.file(noChecksConfig, *CONFIG_PATH)
		}
		gradle.file(failingConfig, "module", *CONFIG_PATH)

		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test fun `uses local module pmd config over rootProject pmd config`() {
		gradle.file(noChecksConfig, *CONFIG_PATH)
		gradle.file(failingConfig, "module", * CONFIG_PATH)

		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	private fun executeBuildAndVerifyMissingContentCheckWasRun() {
		@Language("gradle")
		val script = """
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.pmd'
				apply plugin: 'pmd' // TODO figure out why this is needed to set toolVersion when Pmd task works anyway
				pmd {
					toolVersion = '5.6.1' // Gradle 4.10.3
				}
				tasks.withType(${Pmd::class.java.name}) {
					// output all violations to the console so that we can parse the results
					consoleOutput = true
				}
			}
		""".trimIndent()

		gradle.file(failingContent, "module", "src", "main", "java", "Pmd.java")
		// see also @Test/given for configuration file location setup

		val result = gradle.runFailingBuild {
			basedOn("android-single_module")
			run(script, ":module:pmdDebug")
		}

		// build should only fail if failing config wins the preference,
		// otherwise it's BUILD SUCCESSFUL or RuleSetNotFoundException: Can't find resource "....xml" for rule "null".
		assertEquals(TaskOutcome.FAILED, result.task(":module:pmdDebug")!!.outcome)
		assertThat(result.failReason, containsString("1 PMD rule violations were found."))
		result.assertHasOutputLine(
			Regex(""".*src.main.java.Pmd\.java:1:\s+All classes and interfaces must belong to a named package""")
		)
	}
}
