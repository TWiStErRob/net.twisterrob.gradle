package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test

class GlobalTestFinalizerTaskTest {

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	@Test void "gathers results from root app"() {
		given:
		@Language('java')
		def testFile = """\
			import org.junit.*;

			@SuppressWarnings("NewMethodNamingConvention")
			public class Tests {
				@Test public void success1() {}
				@Test public void success2() {}
				@Test public void success3() {}

				@Test(expected = RuntimeException.class) public void fail1() {}
				@Test public void fail2() { Assert.fail("failure message"); }
				
				@Test @Ignore public void ignored1() { Assert.fail("should not be executed"); }
				@Test public void ignored2() { Assume.assumeTrue(false); }
			}
		""".stripIndent()
		gradle.file(testFile, 'src', 'test', 'java', 'Tests.java')

		@Language('gradle')
		def script = """\
			dependencies {
				testImplementation 'junit:junit:4.12'
			}
			task('tests', type: ${GlobalTestFinalizerTask.name})
		""".stripIndent()

		when:
		def result = gradle.basedOn('android-root_app')
		                   .run(script, 'test', 'tests')
		                   .buildAndFail()

		then:
		assert result.task(':test').outcome == TaskOutcome.SUCCESS
		assert result.task(':tests').outcome == TaskOutcome.FAILED
		result.assertHasOutputLine(/> There were ${2 + 2} failing tests. See the report at: .*/)
	}
}
