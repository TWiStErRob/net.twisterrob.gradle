package net.twisterrob.gradle.quality.tasks

import junit.runner.Version
import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoTask
import net.twisterrob.gradle.test.minus
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import net.twisterrob.gradle.test.tasksIn
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.either
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
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

		if (GradleVersion.version("8.0") <= gradle.gradleVersion.baseVersion) {
			assertEquals(TaskOutcome.NO_SOURCE, result.task(":testReport")!!.outcome)
		} else {
			assertEquals(TaskOutcome.SUCCESS, result.task(":testReport")!!.outcome)
		}
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

	@Test fun `gathers results from subprojects`() {
		val modules = arrayOf(
			":module1",
			":module2",
			":module2:sub1",
			":module2:sub2",
			":module3:sub1",
			":module3:sub2"
		)
		val applyTo = arrayOf(":module1", ":module2", ":module2:sub1", ":module3:sub2")
		modules.forEach {
			gradle.settingsFile.appendText("include '${it}'${endl}")

			@Language("gradle")
			val subProject = """
				apply plugin: 'com.android.library'
				dependencies {
					testImplementation 'junit:junit:${Version.id()}'
				}
			""".trimIndent()
			@Language("xml")
			val manifest = """
				<manifest package="project${it.replace(":", ".")}" />
			""".trimIndent()

			val subPath = it.split(":").toTypedArray()
			gradle.file(subProject, *subPath, "build.gradle")
			gradle.file(manifest, *subPath, "src", "main", "AndroidManifest.xml")
			if (it in applyTo) {
				gradle.file(testFile, *subPath, "src", "test", "java", "Tests.java")
			}
		}

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.quality'
		""".trimIndent()

		val result = gradle.runFailingBuild {
			basedOn("android-multi_module")
			run(script, "testDebugUnitTest", "testReport")
		}

		applyTo.forEach { module ->
			assertEquals(TaskOutcome.SUCCESS, result.task("$module:testDebugUnitTest")!!.outcome)
		}
		(modules - applyTo).forEach { module ->
			assertEquals(TaskOutcome.NO_SOURCE, result.task("$module:testDebugUnitTest")!!.outcome)
		}
		assertEquals(TaskOutcome.FAILED, result.task(":testReport")!!.outcome)
		result.assertHasOutputLine(Regex("""> There were ${applyTo.size * 3} failing tests. See the report at: .*"""))

		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "testDebugUnitTest")
		assertThat(allTasks - tasks - ":testReport", not(hasItem(startsWith("test"))))
	}
}
