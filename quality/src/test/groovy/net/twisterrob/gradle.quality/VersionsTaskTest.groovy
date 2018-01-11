package net.twisterrob.gradle.quality

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test

class VersionsTaskTest {

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	@Test void "print missing versions"() {
		given:
		@Language('gradle')
		def script = """\
			task('qualityVersions', type: ${VersionsTask.name})
		""".stripIndent()

		when:
		def result = gradle.run(script, 'qualityVersions').build()

		then:
		assert result.task(':qualityVersions').outcome == TaskOutcome.SUCCESS
		result.assertHasOutputLine(/Gradle version: .+/)
		result.assertHasOutputLine(/Checkstyle version: 'checkstyle' plugin not applied/)
		result.assertHasOutputLine(/PMD version: 'pmd' plugin not applied/)
		result.assertHasOutputLine(/FindBugs version: 'findbugs' plugin not applied/)
	}

	@Test void "print checkstyle version"() {
		given:
		gradle.setGradleVersion("4.2.1")

		@Language('gradle')
		def script = """\
			apply plugin: 'checkstyle'
			task('qualityVersions', type: ${VersionsTask.name})
		""".stripIndent()

		when:
		def result = gradle.run(script, 'qualityVersions').build()

		then:
		assert result.task(':qualityVersions').outcome == TaskOutcome.SUCCESS
		result.assertHasOutputLine(/Gradle version: 4\.2\.1/)
		result.assertHasOutputLine(/Checkstyle version: 6\.19/)
	}

	@Test void "print pmd version"() {
		given:
		gradle.setGradleVersion("4.2.1")

		@Language('gradle')
		def script = """\
			apply plugin: 'pmd'
			task('qualityVersions', type: ${VersionsTask.name})
		""".stripIndent()

		when:
		def result = gradle.run(script, 'qualityVersions').build()

		then:
		assert result.task(':qualityVersions').outcome == TaskOutcome.SUCCESS
		result.assertHasOutputLine(/Gradle version: 4\.2\.1/)
		result.assertHasOutputLine(/PMD version: 5\.6\.1/)
	}

	@Test void "print findbugs version"() {
		given:
		gradle.setGradleVersion("4.2.1")

		@Language('gradle')
		def script = """\
			apply plugin: 'findbugs'
			task('qualityVersions', type: ${VersionsTask.name})
		""".stripIndent()

		when:
		def result = gradle.run(script, 'qualityVersions').build()

		then:
		assert result.task(':qualityVersions').outcome == TaskOutcome.SUCCESS
		result.assertHasOutputLine(/Gradle version: 4\.2\.1/)
		result.assertHasOutputLine(/FindBugs version: 3\.0\.1/)
	}
}
