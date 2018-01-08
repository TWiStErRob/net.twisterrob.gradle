package net.twisterrob.gradle.quality

import net.twisterrob.gradle.test.GradleRunnerRule
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test

class ValidateViolationsTaskTest {

	private static final List<String> CONFIG_PATH = [ 'config', 'checkstyle', 'checkstyle.xml' ]

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	@Test void "get total violation counts"() {
		given:
		@Language('gradle')
		def settings = """\
			include ':module'
		""".stripIndent()
		gradle.settingsFile() << settings

		@Language('gradle')
		def module = """\
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'com.android.library'
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

		gradle.file(gradle.templateFile('checkstyle-mandatory-header-content.xml').text, CONFIG_PATH)

		def rootProject = gradle.templateFile('android-multiproject-root.gradle').text
		rootProject += """
			task('printViolationCount', type: ${ValidateViolationsTask.name})
		""".stripIndent()


		when:
		def result = gradle.run(rootProject, 'checkstyleAll', 'printViolationCount').build()

		then:
		result.assertHasOutputLine(/Violations: 2/)
	}
}
