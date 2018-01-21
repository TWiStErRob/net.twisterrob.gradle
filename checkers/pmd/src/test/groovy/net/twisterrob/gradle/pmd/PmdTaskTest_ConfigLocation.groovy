package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.api.plugins.quality.Pmd
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PmdTaskTest_ConfigLocation {

	private static final List<String> CONFIG_PATH = [ 'config', 'pmd', 'pmd.xml' ]

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	private String noChecksConfig
	private String failingConfig
	private String failingContent

	@Before void setUp() {
		noChecksConfig = gradle.templateFile('pmd-empty.xml').text
		failingConfig = gradle.templateFile('pmd-simple_failure.xml').text
		failingContent = gradle.templateFile('pmd-simple_failure.java').text
	}

	@Test void "uses rootProject pmd config as a fallback"() {
		given:
		gradle.file(failingConfig, CONFIG_PATH)
		//noinspection GroovyConstantIfStatement do not set up, we want it to use rootProject's
		if (false) {
			gradle.file(noChecksConfig, [ 'module' ] + CONFIG_PATH)
		}

		test:
		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test void "uses local module pmd config if available"() {
		given:
		//noinspection GroovyConstantIfStatement do not set up rootProject's, we want to see if works without as well
		if (false) {
			gradle.file(noChecksConfig, CONFIG_PATH)
		}
		gradle.file(failingConfig, [ 'module' ] + CONFIG_PATH)

		test:
		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test void "uses local module pmd config over rootProject pmd config"() {
		given:
		gradle.file(noChecksConfig, CONFIG_PATH)
		gradle.file(failingConfig, [ 'module' ] + CONFIG_PATH)

		test:
		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	private void executeBuildAndVerifyMissingContentCheckWasRun() {
		given:
		@Language('gradle')
		def script = """\
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.pmd'
				tasks.withType(${Pmd.name}) {
					// output all violations to the console so that we can parse the results
					consoleOutput = true
				}
			}
		""".stripIndent()

		gradle.file(failingContent, 'module', 'src', 'main', 'java', 'Pmd.java')
		// see also @Test/given for configuration file location setup

		when:
		def result = gradle.basedOn('android-single_module')
		                   .run(script, ':module:pmdDebug')
		                   .buildAndFail()

		then:
		// build should only fail if failing config wins the preference,
		// otherwise it's BUILD SUCCESSFUL or RuleSetNotFoundException: Can't find resource '....xml' for rule 'null'.
		assert result.task(':module:pmdDebug').outcome == TaskOutcome.FAILED
		assert result.failReason() =~ /1 PMD rule violations were found./
		result.assertHasOutputLine(
				/.*src.main.java.Pmd\.java:1:\s+All classes and interfaces must belong to a named package/)
	}
}
