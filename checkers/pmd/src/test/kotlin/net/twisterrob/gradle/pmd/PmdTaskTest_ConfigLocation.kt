package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.failReason
import org.gradle.api.plugins.quality.Pmd
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PmdTaskTest_ConfigLocation {

	companion object {

		val CONFIG_PATH = arrayOf("config", "pmd", "pmd.xml")
	}

	@Rule @JvmField val gradle = GradleRunnerRule()

	private lateinit var noChecksConfig: String
	private lateinit var failingConfig: String
	private lateinit var failingContent: String

	@Before fun setUp() {
		noChecksConfig = gradle.templateFile("pmd-empty.xml").readText()
		failingConfig = gradle.templateFile("pmd-simple_failure.xml").readText()
		failingContent = gradle.templateFile("pmd-simple_failure.java").readText()
	}

	@Test fun `uses rootProject pmd config as a fallback`() {
		`given`@
		gradle.file(failingConfig, *CONFIG_PATH)
		@Suppress("ConstantConditionIf") // do not set up, we want it to use rootProject's
		if (false) {
			gradle.file(noChecksConfig, "module", * CONFIG_PATH)
		}

		`test`@
		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test fun `uses local module pmd config if available`() {
		`given`@
		@Suppress("ConstantConditionIf") // do not set up rootProject's, we want to see if works without as well
		if (false) {
			gradle.file(noChecksConfig, *CONFIG_PATH)
		}
		gradle.file(failingConfig, "module", *CONFIG_PATH)

		`test`@
		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test fun `uses local module pmd config over rootProject pmd config`() {
		`given`@
		gradle.file(noChecksConfig, *CONFIG_PATH)
		gradle.file(failingConfig, "module", * CONFIG_PATH)

		`test`@
		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	private fun executeBuildAndVerifyMissingContentCheckWasRun() {
		`given`@
		@Language("gradle")
		val script = """
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.pmd'
				tasks.withType(${Pmd::class.java.name}) {
					// output all violations to the console so that we can parse the results
					consoleOutput = true
				}
			}
		""".trimIndent()

		gradle.file(failingContent, "module", "src", "main", "java", "Pmd.java")
		// see also @Test/given for configuration file location setup

		val result: BuildResult
		`when`@
		result = gradle
				.basedOn("android-single_module")
				.run(script, ":module:pmdDebug")
				.buildAndFail()

		`then`@
		// build should only fail if failing config wins the preference,
		// otherwise it's BUILD SUCCESSFUL or RuleSetNotFoundException: Can't find resource "....xml" for rule "null".
		assertEquals(TaskOutcome.FAILED, result.task(":module:pmdDebug")!!.outcome)
		assertThat(result.failReason, containsString("1 PMD rule violations were found."))
		result.assertHasOutputLine(
				""".*src.main.java.Pmd\.java:1:\s+All classes and interfaces must belong to a named package""".toRegex())
	}
}
