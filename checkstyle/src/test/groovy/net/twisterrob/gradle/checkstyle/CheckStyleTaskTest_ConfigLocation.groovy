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

	@Before void setUp() {
		@Language('gradle')
		def settings = """\
			include ':module'
		""".stripIndent()
		gradle.settingsFile() << settings

		@Language('gradle')
		def module = """\
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'com.android.library'
			tasks.withType(org.gradle.api.plugins.quality.Checkstyle) {
				// output all violations to the console so that we can parse the results
				showViolations = true
			}
		""".stripIndent()
		gradle.file(module, 'module', 'build.gradle')

		@Language('xml')
		def manifest = """\
			<manifest package="module" />
		"""
		gradle.file(manifest, 'module', 'src', 'main', 'AndroidManifest.xml')

		@Language('text')
		def empty = ''
		gradle.file(empty, 'module', 'src', 'main', 'java', 'file')
	}

	@Test void "uses rootProject checkstyle config as a fallback"() {
		given:
		gradle.file(gradle.templateFile('checkstyle-mandatory-header-content.xml').text, CONFIG_PATH)
		//noinspection GroovyConstantIfStatement do not set up, we want it to use rootProject's
		if (false) {
			gradle.file(gradle.templateFile('checkstyle-empty.xml').text, [ 'module' ] + CONFIG_PATH)
		}

		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test void "uses local module checkstyle config if available"() {
		given:
		//noinspection GroovyConstantIfStatement do not set up rootProject's, we want to see if works without as well
		if (false) {
			gradle.file(gradle.templateFile('checkstyle-empty.xml').text, CONFIG_PATH)
		}
		gradle.file(gradle.templateFile('checkstyle-mandatory-header-content.xml').text, [ 'module' ] + CONFIG_PATH)

		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	@Test void "uses local module checkstyle config over rootProject checkstyle config"() {
		given:
		gradle.file(gradle.templateFile('checkstyle-empty.xml').text, CONFIG_PATH)
		gradle.file(gradle.templateFile('checkstyle-mandatory-header-content.xml').text, [ 'module' ] + CONFIG_PATH)

		executeBuildAndVerifyMissingContentCheckWasRun()
	}

	private void executeBuildAndVerifyMissingContentCheckWasRun() {
		given:
		// see also @Before for basic project setup
		// see also @Test/given for test setup
		def rootProject = gradle.templateFile('android-multiproject-root.gradle').text

		when:
		def result = gradle.run(rootProject, ':module:checkstyleDebug').buildAndFail()

		then:
		// build should only fail if checkstyle-mandatory-header-content.xml wins the preference,
		// otherwise it's BUILD SUCCESSFUL or CheckstyleException: Unable to find: ...xml
		assert result.task(':module:checkstyleDebug').outcome == TaskOutcome.FAILED
		assert result.failReason() =~ /Checkstyle rule violations were found/
		result.assertHasOutputLine(/.*src.main.java.file:1: .*? \[Header]/)
	}
}
