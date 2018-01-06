package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class CheckStylePluginTest {

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	@BeforeClass static void setUp() {
//		System.properties['java.io.tmpdir'] = $/P:\temp/$
	}

	@Test void "applies without a hitch to an empty project"() {
		given:
		@Language('gradle')
		def script = """
			apply plugin: 'net.twisterrob.checkstyle'
		""".stripIndent()

		when:
		def result = gradle.run(script, 'checkstyle').buildAndFail()

		then:
		assert result.failReason() =~ /^Task 'checkstyle' not found/
	}

	@Ignore
	@Test void "applies without a hitch to a Java project"() {
		given:
		@Language('gradle')
		def script = """
			apply plugin: 'java'
			apply plugin: 'net.twisterrob.checkstyle'
		""".stripIndent()

		when:
		def result = gradle.run(script, 'checkstyle').buildAndFail()

		then:
		assert result.failReason() =~
				/^Task 'checkstyle' is ambiguous in root project '.*?'. Candidates are: 'checkstyleMain', 'checkstyleTest'.$/
	}

	@Test void "applies without a hitch to an Android project"() {
		given:
		@Language('gradle')
		def script = """
			// it's all in the template
		""".stripIndent()

		when:

		def result = gradle.basedOn('simpleAndroid', this)
		                   .run(script, 'checkstyleAll')
		                   .build()

		then:
		assert result.task(':checkstyleAll').outcome == TaskOutcome.SUCCESS
		assert result.task(':checkstyleDebug').outcome == TaskOutcome.SUCCESS
		assert result.task(':checkstyleRelease').outcome == TaskOutcome.SUCCESS
	}
}
