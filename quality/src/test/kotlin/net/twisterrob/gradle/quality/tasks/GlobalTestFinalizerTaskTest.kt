package net.twisterrob.gradle.quality.tasks

import junit.runner.Version
import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoTask
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.either
import org.hamcrest.Matchers.hasSize
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

	@Test fun `gathers results from empty project`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.quality'
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "test", "testReport")
		}

		assertEquals(TaskOutcome.UP_TO_DATE, result.task(":test")!!.outcome)
		assertEquals(TaskOutcome.SUCCESS, result.task(":testReport")!!.outcome)
		result.assertNoOutputLine(Regex(""".*failing tests.*"""))
		result.assertNoOutputLine(Regex(""".*See the report at.*"""))
	}

	@Test fun `does not execute or compile tests`() {
		gradle.file(testFile, "src", "test", "java", "Tests.java")

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.quality'
			dependencies {
				testImplementation 'junit:junit:${Version.id()}'
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "testReport")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":testReport")!!.outcome)
		assertThat(result.tasks, hasSize(1))
		result.assertNoOutputLine(Regex(""".*failing tests.*"""))
		result.assertNoOutputLine(Regex(""".*See the report at.*"""))
	}

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

	@Test fun `test tasks fail without executing testReport when executing explicitly`() {
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
			run(script, "testDebugUnitTest")
		}

		assertEquals(TaskOutcome.FAILED, result.task(":testDebugUnitTest")!!.outcome)
		result.assertNoTask(":testReport")
		result.assertHasOutputLine(Regex("""> There were failing tests. See the report at: .*"""))
	}

	@Test fun `test tasks fail without executing testReport when executing indirectly`() {
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
			run(script, "test")
		}

		assertThat(
			result.taskPaths(TaskOutcome.FAILED),
			either(contains(":testDebugUnitTest")).or(contains(":testReleaseUnitTest"))
		)
		result.assertNoTask(":testReport")
		result.assertHasOutputLine(Regex("""> There were failing tests. See the report at: .*"""))
	}
}
