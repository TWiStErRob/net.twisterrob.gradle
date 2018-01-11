package net.twisterrob.gradle.quality

import net.twisterrob.gradle.test.GradleRunnerRule
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test

import java.util.regex.Pattern

class ValidateViolationsTaskTest {

	private static final List<String> CONFIG_PATH = [ 'config', 'checkstyle', 'checkstyle.xml' ]
	private static final List<String> MANIFEST_PATH = [ 'src', 'main', 'AndroidManifest.xml' ]
	private static final List<String> SOURCE_PATH = [ 'src', 'main', 'java' ]

	private static final Pattern VIOLATION_PATTERN = Pattern.compile(/([A-Z][a-zA-Z0-9_]+?)_(\d).java/)

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
			task('printViolationCount', type: ${ValidateViolationsTask.name}) {
				action = {Map<String, Map<String, List<se.bjurr.violations.lib.model.Violation>>> results ->
					def count = results.values().sum { map ->
						 map.values().sum { list -> list.size() }
					}
					println "Violations: \${count}"
				}
			}
		""".stripIndent()

		when:
		def result = gradle.basedOn('android-single_module')
		                   .run(script, 'checkstyleAll', 'printViolationCount')
		                   .build()

		then:
		result.assertHasOutputLine(/Violations: 1/)
	}

	@Test void "get per module violation counts"() {
		given:
		def template = gradle.templateFile('checkstyle-multiple_violations/checkstyle-template.xml').text
		def dir = gradle.templateFile('checkstyle-multiple_violations')
		int totalViolations = 0
		dir.listFiles().sort().each {File file ->
			println "Building module from ${file}"
			def match = file.name =~ VIOLATION_PATTERN
			if (match.matches()) {
				def checkName = match.group(1) as String
				totalViolations += match.group(2) as int
				def checkstyleXmlContents = template.replace('${CheckName}', checkName)
				gradle.file(checkstyleXmlContents, [ checkName ] + CONFIG_PATH)
				gradle.file("""<manifest package="checkstyle.${checkName}" />""", [ checkName ] + MANIFEST_PATH)
				gradle.file(file.text, [ checkName ] + SOURCE_PATH + [ file.name ])
				gradle.settingsFile() << "include ':${checkName}'${System.lineSeparator()}"
			} else {
				// skip
			}
		}

		@Language('gradle')
		def script = """\
			subprojects {
				apply plugin: 'com.android.library'
				apply plugin: 'net.twisterrob.checkstyle'
			}
			task('printViolationCounts', type: ${ValidateViolationsTask.name}) {
				action = {Map<String, Map<String, List<se.bjurr.violations.lib.model.Violation>>> results ->
					results.each {module, byVariant ->
						println "\\t\${module}:"
						byVariant.each {variant, violations ->
							println "\\t\\t\${variant}: \${violations.size()}"
						}
					}
				}
			}
		""".stripIndent()

		when:
		def result = gradle.basedOn('android-multi_module')
		                   .run(script, 'checkstyleAll', 'printViolationCounts')
		                   .build()

		then:
		assert result.output.contains("""\
		:printViolationCounts
			:EmptyBlock:
				debug+release: 3
			:MemberName:
				debug+release: 2
			:UnusedImports:
				debug+release: 4
		""".stripIndent().replaceAll("\r?\n", System.lineSeparator()))
	}
}
