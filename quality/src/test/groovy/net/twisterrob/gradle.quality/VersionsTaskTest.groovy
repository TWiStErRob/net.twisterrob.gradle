package net.twisterrob.gradle.quality

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class VersionsTaskTest {

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	@Test void "print missing versions"() {
		given:
		@Language('gradle')
		def script = """
			task('qualityVersions', type: ${VersionsTask.name})
		""".stripIndent()

		when:
		def result = gradle.run(script, 'qualityVersions').build()

		then:
		assert result.task(':qualityVersions').outcome == TaskOutcome.SUCCESS
		assert result.output =~ /(?m)^Gradle version: .+$/
		assert result.output =~ /(?m)^Checkstyle version: 'checkstyle' plugin not applied$/
		assert result.output =~ /(?m)^PMD version: 'pmd' plugin not applied$/
		assert result.output =~ /(?m)^FindBugs version: 'findbugs' plugin not applied$/
	}

	@Test void "print checkstyle version"() {
		given:
		gradle.setGradleVersion("4.2.1")

		@Language('gradle')
		def script = """
			apply plugin: 'checkstyle'
			task('qualityVersions', type: ${VersionsTask.name})
		""".stripIndent()

		when:
		def result = gradle.run(script, 'qualityVersions').build()

		then:
		assert result.task(':qualityVersions').outcome == TaskOutcome.SUCCESS
		assert result.output =~ /(?m)^Gradle version: 4.2.1$/
		assert result.output =~ /(?m)^Checkstyle version: 6.19$/
	}
}
