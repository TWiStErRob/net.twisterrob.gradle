package net.twisterrob.gradle.test

import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test

class GradleRunnerRuleTest {

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	@Test void "gradle script test"() {
		given:
		@Language('gradle')
		def script = """\
			println 'Hello World'
		""".stripIndent()

		when:
		def result = gradle.run(script).build()

		then:
		assert result.output =~ /(?m)^Hello World$/
	}

	@Test void "gradle task test"() {
		given:
		@Language('gradle')
		def script = """\
			task test {
				doLast {
				    println 'Hello World'
				}
			}
		""".stripIndent()

		when:
		def result = gradle.run(script, 'test').build()

		then:
		assert result.task(':test').outcome == TaskOutcome.SUCCESS
		assert result.output =~ /(?m)^Hello World$/
	}

	@Test void "test with file"() {
		given:
		@Language('xml')
		def configFileContents = """\
			<root>
				<element attr="value" />
			</root>
		""".stripIndent()
		gradle.file(configFileContents, 'config/file.xml')

		@Language('gradle')
		def script = """\
			task printConfigFile {
				def configFile = rootProject.file('config/file.xml')
				inputs.file configFile
				doLast {
					println configFile.text
				}
			}
		""".stripIndent()

		when:
		def result = gradle.run(script, 'printConfigFile').build()

		then:
		assert result.task(':printConfigFile').outcome == TaskOutcome.SUCCESS
		assert result.output.contains(configFileContents)
	}
}
