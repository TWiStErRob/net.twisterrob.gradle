package net.twisterrob.gradle.quality.tasks

import junit.runner.Version
import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertFailed
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoSource
import net.twisterrob.gradle.test.assertNoTask
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.assertUpToDate
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
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "test", "testReport")
		}

		result.assertUpToDate(":test")
		if (GradleVersion.version("8.0") <= gradle.gradleVersion.baseVersion) {
			result.assertNoSource(":testReport")
		} else {
			result.assertSuccess(":testReport")
		}
		result.assertNoOutputLine(Regex(""".*failing tests.*"""))
		result.assertNoOutputLine(Regex(""".*See the report at.*"""))
	}

	@Test fun `does not execute or compile tests`() {
		gradle.file(testFile, "src", "test", "java", "Tests.java")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
			dependencies {
				testImplementation 'junit:junit:${Version.id()}'
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "testReport")
		}

		if (GradleVersion.version("8.0") <= gradle.gradleVersion.baseVersion) {
			result.assertNoSource(":testReport")
		} else {
			result.assertSuccess(":testReport")
		}
		assertThat(result.tasks, hasSize(1))
		result.assertNoOutputLine(Regex(""".*failing tests.*"""))
		result.assertNoOutputLine(Regex(""".*See the report at.*"""))
	}

	@Test fun `gathers results from root app`() {
		gradle.file(testFile, "src", "test", "java", "Tests.java")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
			dependencies {
				testImplementation 'junit:junit:${Version.id()}'
			}
		""".trimIndent()

		val result = gradle.runFailingBuild {
			basedOn("android-root_app")
			run(script, "test", "testReport")
		}

		result.assertSuccess(":test")
		result.assertFailed(":testReport")
		result.assertHasOutputLine(Regex("""> There were ${3 + 3} failing tests. See the report at: .*"""))
	}

	@Test fun `test tasks fail without executing testReport when executing explicitly`() {
		gradle.file(testFile, "src", "test", "java", "Tests.java")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
			dependencies {
				testImplementation 'junit:junit:${Version.id()}'
			}
		""".trimIndent()

		val result = gradle.runFailingBuild {
			basedOn("android-root_app")
			run(script, "testDebugUnitTest")
		}

		result.assertFailed(":testDebugUnitTest")
		result.assertNoTask(":testReport")
		result.assertHasOutputLine(Regex("""> There were failing tests. See the report at: .*"""))
	}

	@Test fun `test tasks fail without executing testReport when executing indirectly`() {
		gradle.file(testFile, "src", "test", "java", "Tests.java")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
			dependencies {
				testImplementation 'junit:junit:${Version.id()}'
			}
		""".trimIndent()

		val result = gradle.runFailingBuild {
			basedOn("android-root_app")
			run(script, "test")
		}

		assertThat(
			result.taskPaths(TaskOutcome.FAILED), // Parallel execution can cause any combination of failures.
			either(contains(":testDebugUnitTest", ":testReleaseUnitTest"))
				.or(contains(":testDebugUnitTest"))
				.or(contains(":testReleaseUnitTest"))
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
		modules.forEach { modulePath ->
			gradle.settingsFile.appendText("""include("${modulePath}")${System.lineSeparator()}""")

			@Language("gradle")
			val subProject = """
				plugins {
					id("com.android.library")
				}
				android.namespace = "project${modulePath.replace(":", ".")}"
				dependencies {
					testImplementation 'junit:junit:${Version.id()}'
				}
			""".trimIndent()

			val subPath = modulePath.split(":").toTypedArray()
			gradle.file(subProject, *subPath, "build.gradle")
			if (modulePath in applyTo) {
				gradle.file(testFile, *subPath, "src", "test", "java", "Tests.java")
			}
		}

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
		""".trimIndent()

		val result = gradle.runFailingBuild {
			basedOn("android-multi_module")
			run(script, "testDebugUnitTest", "testReport")
		}

		applyTo.forEach { module ->
			result.assertSuccess("$module:testDebugUnitTest")
		}
		(modules - applyTo).forEach { module ->
			result.assertNoSource("$module:testDebugUnitTest")
		}
		result.assertFailed(":testReport")
		result.assertHasOutputLine(Regex("""> There were ${applyTo.size * 3} failing tests. See the report at: .*"""))

		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "testDebugUnitTest")
		assertThat(allTasks - tasks - ":testReport", not(hasItem(startsWith("test"))))
	}
}
