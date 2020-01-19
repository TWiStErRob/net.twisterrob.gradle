package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import org.gradle.testkit.runner.TaskOutcome.SKIPPED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.runners.model.Statement
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal val GradleRunnerRule.root get() = this.settingsFile.parentFile!!

internal fun GradleRunnerRule.delete(path: String) {
	assertTrue(
		root.resolve(path).deleteRecursively(),
		"Cannot delete $path"
	)
}

internal fun BuildResult.assertNoTask(taskPath: String) = assertNull(task(taskPath))

/**
 * Assert that the task exists and that it ran to completion with success.
 * Note: this means that [TaskOutcome.UP_TO_DATE] and [TaskOutcome.NO_SOURCE] is not "success"!
 */
internal fun BuildResult.assertSuccess(taskPath: String) =
	assertOutcome(taskPath, SUCCESS)

internal fun BuildResult.assertFailed(taskPath: String) =
	assertOutcome(taskPath, FAILED)

internal fun BuildResult.assertSkipped(taskPath: String) =
	assertOutcome(taskPath, SKIPPED)

internal fun BuildResult.assertUpToDate(taskPath: String) =
	assertOutcome(taskPath, UP_TO_DATE)

internal fun BuildResult.assertFromCache(taskPath: String) =
	assertOutcome(taskPath, FROM_CACHE)

internal fun BuildResult.assertNoSource(taskPath: String) =
	assertOutcome(taskPath, NO_SOURCE)

internal fun BuildResult.assertOutcome(taskPath: String, outcome: TaskOutcome) {
	@Suppress("ReplaceSingleLineLet")
	val task = task(taskPath)
		.let { assertNotNull(it, "${taskPath} task not found") }
	assertEquals(outcome, task.outcome)
}

/**
 * Helper to allow SAM-like behavior for [Statement] abstract class.
 */
@Suppress("TestFunctionName")
internal inline fun Statement(crossinline block: () -> Unit) =
	object : Statement() {
		override fun evaluate() {
			block()
		}
	}
