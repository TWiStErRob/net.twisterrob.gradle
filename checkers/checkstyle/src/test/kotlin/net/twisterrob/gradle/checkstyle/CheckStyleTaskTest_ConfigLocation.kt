package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.failReason
import net.twisterrob.gradle.test.runFailingBuild
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/**
 * @see CheckStyleTaskCreator
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class CheckStyleTaskTest_ConfigLocation {

	companion object {
		val CONFIG_PATH = arrayOf("config", "checkstyle", "checkstyle.xml")
	}

	private lateinit var gradle: GradleRunnerRule

	private lateinit var noChecksConfig: String
	private lateinit var failingConfig: String
	private lateinit var failingContent: String

	@BeforeEach fun setUp() {
		noChecksConfig = gradle.templateFile("checkstyle-empty.xml").readText()
		failingConfig = gradle.templateFile("checkstyle-simple_failure.xml").readText()
		failingContent = gradle.templateFile("checkstyle-simple_failure.xml").readText()
	}

	@Test fun `uses rootProject checkstyle config as a fallback`() {
		gradle.file(failingConfig, *CONFIG_PATH)
		@Suppress("ConstantConditionIf") // do not set up, we want it to use rootProject's
		if (false) {
			gradle.file(noChecksConfig, "module", *CONFIG_PATH)
		}

		executeBuild().verifyMissingContentCheckWasRun()
	}

	@Test fun `uses local module checkstyle config if available`() {
		@Suppress("ConstantConditionIf") // do not set up rootProject's, we want to see if works without as well
		if (false) {
			gradle.file(noChecksConfig, *CONFIG_PATH)
		}
		gradle.file(failingConfig, "module", *CONFIG_PATH)

		executeBuild().verifyMissingContentCheckWasRun()
	}

	@Test fun `uses local module checkstyle config over rootProject checkstyle config`() {
		gradle.file(noChecksConfig, *CONFIG_PATH)
		gradle.file(failingConfig, "module", *CONFIG_PATH)

		executeBuild().verifyMissingContentCheckWasRun()
	}

	@Test fun `warns about missing configuration`() {
		@Suppress("ConstantConditionIf") // Do not set up, we want it to not exist.
		if (false) {
			gradle.file(noChecksConfig, *CONFIG_PATH)
		}

		val result = executeBuild()
		assertEquals(TaskOutcome.FAILED, result.task(":module:checkstyleDebug")!!.outcome)
		assertThat(result.failReason, containsString("Unable to create Root Module: config"))
		result.assertHasOutputLine("""While auto-configuring configFile for task ':module:checkstyleDebug', there was no configuration found at:""")
	}

	private fun executeBuild(): BuildResult {
		@Language("gradle")
		val script = """
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.checkstyle'
				tasks.withType(${Checkstyle::class.java.name}) {
					// output all violations to the console so that we can parse the results
					showViolations = true
				}
			}
		""".trimIndent()

		gradle.file(failingContent, "module", "src", "main", "java", "Checkstyle.java")
		// see also @Test/given for configuration file location setup

		return gradle.runFailingBuild {
			basedOn("android-single_module")
			run(script, ":module:checkstyleDebug")
		}
	}
}

private fun BuildResult.verifyMissingContentCheckWasRun() {
	// build should only fail if failing config wins the preference,
	// otherwise it's BUILD SUCCESSFUL or CheckstyleException: Unable to find: ...xml
	assertEquals(TaskOutcome.FAILED, this.task(":module:checkstyleDebug")!!.outcome)

	assertThat(this.failReason, containsString("Checkstyle rule violations were found"))
	this.assertHasOutputLine(Regex(""".*src.main.java.Checkstyle\.java:1: .*? \[Header]"""))
	this.assertNoOutputLine(Regex("""While auto-configuring configFile for task '.*"""))
}
