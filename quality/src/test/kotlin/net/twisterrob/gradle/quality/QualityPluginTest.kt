package net.twisterrob.gradle.quality

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.quality.tasks.GlobalLintGlobalFinalizerTask
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.runBuild
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.not
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/**
 * @see QualityPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class QualityPluginTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `apply violationReportConsole only on root project`() {
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.gradle.plugin.quality'
			}
			tasks.named('violationReportConsole').configure { println("Configuring " + it) }
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-all_kinds")
			run(script, "violationReportConsole")
		}

		assertEquals(SUCCESS, result.task(":violationReportConsole")!!.outcome)
		val allOtherTasks = result.tasks.map { it.path } - ":violationReportConsole"
		assertThat(allOtherTasks, not(hasItems(matchesPattern(""":violationReportConsole$"""))))
		result.assertHasOutputLine(
			"should be configurable without afterEvaluate",
			"Configuring task ':violationReportConsole'"
		)
	}

	@Test fun `apply violationReportHtml only on root project`() {
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.gradle.plugin.quality'
			}
			tasks.named('violationReportHtml').configure { println("Configuring " + it) }
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-all_kinds")
			run(script, "violationReportHtml")
		}

		assertEquals(SUCCESS, result.task(":violationReportHtml")!!.outcome)
		val allOtherTasks = result.tasks.map { it.path } - ":violationReportHtml"
		assertThat(allOtherTasks, not(hasItems(matchesPattern(""":violationReportHtml$"""))))
		result.assertHasOutputLine(
			"should be configurable without afterEvaluate",
			"Configuring task ':violationReportHtml'"
		)
	}

	@Test fun `apply lint only on root project`() {
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.gradle.plugin.quality'
				tasks.withType(${GlobalLintGlobalFinalizerTask::class.qualifiedName}).configureEach {
					println("Added " + it)
				}
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-all_kinds")
			run(script, "lint")
		}

		assertEquals(SUCCESS, result.task(":lint")!!.outcome)
		result.assertHasOutputLine("one task added for finalizer", "Added task ':lint'")
		result.assertNoOutputLine("no other tasks added as finalizer", Regex("""Added task ':(.+?):lint'"""))
	}

	@Test fun `apply lint only when Android does not add lint task`() {
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.gradle.plugin.quality'
				tasks.withType(${GlobalLintGlobalFinalizerTask::class.qualifiedName}).configureEach {
					println("Added " + it)
				}
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "lint")
		}

		assertEquals(SUCCESS, result.task(":lint")!!.outcome)
		result.assertNoOutputLine("no tasks added as finalizer", Regex("""Added task '(.*?):lint'"""))
	}
}
