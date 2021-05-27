package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.failReason
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import org.gradle.api.plugins.quality.Pmd
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(GradleRunnerRuleExtension::class)
class PmdTaskTest_ConfigLocation {

	companion object {

		val CONFIG_PATH = arrayOf("config", "pmd", "pmd.xml")

		@Language("gradle")
		val SCRIPT_CONFIGURE_PMD = """
			import org.gradle.util.GradleVersion
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.pmd'
				apply plugin: 'pmd' // TODO figure out why this is needed to set toolVersion when Pmd task works anyway
				pmd {
					if (GradleVersion.version("6.0.0") <= GradleVersion.current()) {
						//incrementalAnalysis.set(false)
					}
				}
				tasks.withType(${Pmd::class.java.name}) {
					// output all violations to the console so that we can parse the results
					consoleOutput = true
				}
			}
		""".trimIndent()
	}

	private lateinit var gradle: GradleRunnerRule

	private val noChecksConfig: String
		get() = gradle.templateFile("pmd-empty.xml").readText()

	private val failingConfig: String
		get() =
			if (gradle.getGradleVersion() < GradleVersion.version("5.0.0")) {
				gradle.templateFile("pmd-simple_failure-old.xml").readText()
			} else {
				// Gradle 5+ has PMD 6.x embedded, so they emit a deprecation warning if the old config is used (#81).
				gradle.templateFile("pmd-simple_failure.xml").readText()
			}

	private val failingContent: String
		get() = gradle.templateFile("pmd-simple_failure.java").readText()

	@Test fun `uses rootProject pmd config as a fallback`() {
		gradle.file(failingConfig, *CONFIG_PATH)
		@Suppress("ConstantConditionIf") // do not set up, we want it to use rootProject's
		if (false) {
			gradle.file(noChecksConfig, "module", * CONFIG_PATH)
		}

		executeBuild().verifyMissingContentCheckWasRun()
	}

	@Test fun `uses local module pmd config if available`() {
		@Suppress("ConstantConditionIf") // do not set up rootProject's, we want to see if works without as well
		if (false) {
			gradle.file(noChecksConfig, *CONFIG_PATH)
		}
		gradle.file(failingConfig, "module", *CONFIG_PATH)

		executeBuild().verifyMissingContentCheckWasRun()
	}

	@Test fun `uses local module pmd config over rootProject pmd config`() {
		gradle.file(noChecksConfig, *CONFIG_PATH)
		gradle.file(failingConfig, "module", * CONFIG_PATH)

		executeBuild().verifyMissingContentCheckWasRun()
	}

	@Test fun `warns about missing configuration`() {
		@Suppress("ConstantConditionIf") // Do not set up, we want it to not exist.
		if (false) {
			gradle.file(noChecksConfig, *CONFIG_PATH)
		}

		val result = executeBuild()
		assertEquals(TaskOutcome.FAILED, result.task(":module:pmdDebug")!!.outcome)
		assertThat(result.failReason, containsString("No rulesets specified"))
		result.assertHasOutputLine("""While auto-configuring ruleSetFiles for task ':module:pmdDebug', there was no configuration found at:""")
	}

	@Test fun `does not warn about missing configuration when not executed`() {
		@Suppress("ConstantConditionIf") // Do not set up, we want it to not exist.
		if (false) {
			gradle.file(noChecksConfig, *CONFIG_PATH)
		}

		val result = gradle.runBuild {
			basedOn("android-single_module")
			run(SCRIPT_CONFIGURE_PMD, ":module:tasks")
		}
		assertEquals(TaskOutcome.SUCCESS, result.task(":module:tasks")!!.outcome)
		result.assertNoOutputLine(Regex("""While auto-configuring ruleSetFiles for task '.*"""))
	}

	private fun executeBuild(): BuildResult {
		gradle.file(failingContent, "module", "src", "main", "java", "Pmd.java")
		// see also @Test/given for configuration file location setup

		return gradle.runFailingBuild {
			basedOn("android-single_module")
			run(SCRIPT_CONFIGURE_PMD, ":module:pmdDebug")
		}
	}

	private fun BuildResult.verifyMissingContentCheckWasRun() {
		// build should only fail if failing config wins the preference,
		// otherwise it's BUILD SUCCESSFUL or RuleSetNotFoundException: Can't find resource "....xml" for rule "null".
		assertEquals(TaskOutcome.FAILED, this.task(":module:pmdDebug")!!.outcome)
		assertThat(this.failReason, containsString("1 PMD rule violations were found."))
		if (gradle.getGradleVersion() < GradleVersion.version("5.6.0")) {
			this.assertHasOutputLine(
				Regex(""".*src.main.java.Pmd\.java:1:\s+All classes and interfaces must belong to a named package""")
			)
		} else {
			this.assertHasOutputLine(
				Regex(""".*src.main.java.Pmd\.java:1:\s+All classes, interfaces, enums and annotations must belong to a named package""")
			)
		}
		this.assertNoOutputLine(Regex("""While auto-configuring ruleSetFiles for task '.*"""))
	}
}
