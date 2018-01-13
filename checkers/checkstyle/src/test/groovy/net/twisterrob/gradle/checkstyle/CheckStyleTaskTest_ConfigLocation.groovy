package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CheckStyleTaskTest_ConfigLocation {

	private static final List<String> CONFIG_PATH = [ 'config', 'checkstyle', 'checkstyle.xml' ]

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	private String noChecksConfig
	private String headerValidatorConfig

	@Before void setUp() {
		noChecksConfig = gradle.templateFile('checkstyle-empty.xml').text
		headerValidatorConfig = gradle.templateFile('checkstyle-mandatory-header-content.xml').text
	}

	@Test void "uses rootProject checkstyle config as a fallback"() {
		given:
		gradle.file(headerValidatorConfig, CONFIG_PATH)
		//noinspection GroovyConstantIfStatement do not set up, we want it to use rootProject's
		if (false) {
			gradle.file(noChecksConfig, [ 'module' ] + CONFIG_PATH)
		}

		test:
		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test void "uses local module checkstyle config if available"() {
		given:
		//noinspection GroovyConstantIfStatement do not set up rootProject's, we want to see if works without as well
		if (false) {
			gradle.file(noChecksConfig, CONFIG_PATH)
		}
		gradle.file(headerValidatorConfig, [ 'module' ] + CONFIG_PATH)

		test:
		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test void "uses local module checkstyle config over rootProject checkstyle config"() {
		given:
		gradle.file(noChecksConfig, CONFIG_PATH)
		gradle.file(headerValidatorConfig, [ 'module' ] + CONFIG_PATH)

		test:
		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	private void executeBuildAndVerifyMissingContentCheckWasRun() {
		given:
		@Language('gradle')
		def script = """\
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.checkstyle'
				tasks.withType(org.gradle.api.plugins.quality.Checkstyle) {
					// output all violations to the console so that we can parse the results
					showViolations = true
				}
			}
		""".stripIndent()

		@Language('text')
		def empty = ''
		gradle.file(empty, 'module', 'src', 'main', 'java', 'file')
		// see also @Test/given for configuration file location setup

		when:
		def result = gradle.basedOn('android-single_module')
		                   .run(script, ':module:checkstyleDebug')
		                   .buildAndFail()

		then:
		// build should only fail if checkstyle-mandatory-header-content.xml wins the preference,
		// otherwise it's BUILD SUCCESSFUL or CheckstyleException: Unable to find: ...xml
		assert result.task(':module:checkstyleDebug').outcome == TaskOutcome.FAILED
		assert result.failReason() =~ /Checkstyle rule violations were found/
		result.assertHasOutputLine(/.*src.main.java.file:1: .*? \[Header]/)
	}
}
