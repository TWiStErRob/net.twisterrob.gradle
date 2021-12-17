package net.twisterrob.gradle.quality.tasks

import junit.runner.Version
import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.runFailingBuild
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/**
 * @see GlobalTestFinalizerTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class GlobalTestFinalizerTaskTest : BaseIntgTest() {

	companion object {

		private val endl = System.lineSeparator()

		@Suppress("")
		@Language("java")
		private val testFile: String = """
			import org.junit.*;
			
			@SuppressWarnings({
				"NewClassNamingConvention", "NewMethodNamingConvention",
				"ConstantConditions", "JUnitTestMethodInProductSource",
			})
			public class Tests {
				@Test public void success1() {}
				@Test public void success2() {}
				@Test public void success3() {}
			
				@Test(expected = RuntimeException.class) public void fail1() {}
				@Test public void fail2() { Assert.fail("failure message"); }
			
				@SuppressWarnings("ResultOfMethodCallIgnored")
				@Test public void error() { ((String) null).isEmpty(); }
			
				@Test @Ignore public void ignored1() { Assert.fail("should not be executed"); }
				@Test public void ignored2() { Assume.assumeTrue(false); }
			}
		""".trimIndent()
	}

	override lateinit var gradle: GradleRunnerRule

	@Test fun `gathers results from root app`() {
		gradle.file(testFile, "src", "test", "java", "Tests.java")

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.quality'
			dependencies {
				testImplementation 'junit:junit:${Version.id()}'
			}
		""".trimIndent()

		val result = gradle.runFailingBuild {
			basedOn("android-root_app")
			run(script, "test", "testReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":test")!!.outcome)
		assertEquals(TaskOutcome.FAILED, result.task(":testReport")!!.outcome)
		result.assertHasOutputLine(Regex("""> There were ${3 + 3} failing tests. See the report at: .*"""))
	}
}
