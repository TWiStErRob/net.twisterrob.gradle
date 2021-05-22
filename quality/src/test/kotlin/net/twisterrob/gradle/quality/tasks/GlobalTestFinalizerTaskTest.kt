package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.runFailingBuild
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(GradleRunnerRuleExtension::class)
class GlobalTestFinalizerTaskTest {

	private lateinit var gradle: GradleRunnerRule

	@Test fun `gathers results from root app`() {
		@Language("java")
		val testFile = """
			import org.junit.*;

			@SuppressWarnings({"NewMethodNamingConvention", "ConstantConditions"})
			public class Tests {
				@Test public void success1() {}
				@Test public void success2() {}
				@Test public void success3() {}

				@Test(expected = RuntimeException.class) public void fail1() {}
				@Test public void fail2() { Assert.fail("failure message"); }

				@Test @Ignore public void ignored1() { Assert.fail("should not be executed"); }
				@Test public void ignored2() { Assume.assumeTrue(false); }
			}
		""".trimIndent()
		gradle.file(testFile, "src", "test", "java", "Tests.java")

		@Language("gradle")
		val script = """
			dependencies {
				testImplementation 'junit:junit:4.13.2'
			}
			task('tests', type: ${GlobalTestFinalizerTask::class.java.name})
		""".trimIndent()

		val result = gradle.runFailingBuild {
			basedOn("android-root_app")
			run(script, "test", "tests")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":test")!!.outcome)
		assertEquals(TaskOutcome.FAILED, result.task(":tests")!!.outcome)
		result.assertHasOutputLine(Regex("""> There were ${2 + 2} failing tests. See the report at: .*"""))
	}
}
