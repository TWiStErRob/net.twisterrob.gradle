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
		@Language('text')
		def empty = ''
		gradle.file(empty, 'module', 'src', 'main', 'java', 'file')

		gradle.file(gradle.templateFile('checkstyle-mandatory-header-content.xml').text, CONFIG_PATH)

		@Language('gradle')
		def script = """\
			subprojects { // i.e. :module
				apply plugin: 'net.twisterrob.checkstyle'
			}
			task('printViolationCount', type: ${ValidateViolationsTask.name})
		""".stripIndent()

		when:
		def result = gradle.basedOn('android-single_module')
		                   .run(script, 'checkstyleAll', 'printViolationCount')
		                   .build()

		then:
		result.assertHasOutputLine(/Violations: 2/)
	}
}
