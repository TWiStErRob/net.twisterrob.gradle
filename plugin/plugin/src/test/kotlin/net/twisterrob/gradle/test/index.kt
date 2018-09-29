package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal val GradleRunnerRule.root get () = this.settingsFile().parentFile!!

internal fun BuildResult.assertNoTask(taskPath: String) = assertNull(task(taskPath))

/**
 * Assert that the task exists and that it ran to completion with success.
 * Note: this means that UP-TO-DATE and NO-SOURCE will fail!
 */
internal fun BuildResult.assertSuccess(taskPath: String) = assertOutcome(taskPath, SUCCESS)

internal fun BuildResult.assertFailed(taskPath: String) = assertOutcome(taskPath, FAILED)
internal fun BuildResult.assertOutcome(taskPath: String, outcome: TaskOutcome) {
	@Suppress("ReplaceSingleLineLet")
	val task = task(taskPath)
		.let { assertNotNull(it, "${taskPath} task not found") }
	assertEquals(outcome, task.outcome)
}
