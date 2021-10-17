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
import java.io.File
import java.util.Date
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

internal val GradleRunnerRule.root get() = this.settingsFile.parentFile!!

internal fun GradleRunnerRule.delete(path: String) {
	val file = root.resolve(path)
	assertTrue(
		file.deleteRecursively(),
		"Cannot delete $path\n${file.describe()}"
	)
}

internal fun GradleRunnerRule.move(from: String, to: String) {
	val fromFile = root.resolve(from)
	val toFile = root.resolve(to)
	assertTrue(
		toFile.parentFile.isDirectory || toFile.parentFile.mkdirs(),
		"Cannot create folder: ${toFile.parentFile.describe()}"
	)
	assertTrue(
		fromFile.renameTo(toFile),
		"Cannot move $from to $to\n${fromFile.describe()}\n${toFile.describe()}"
	)
}

private fun File.describe(): String = absolutePath +
		", stat=${type() + chmod()}" +
		", size=${length()}" +
		", date=${Date(this.lastModified())}"

private fun File.chmod(): String = "" +
		(if (canRead()) "r" else "-") +
		(if (canWrite()) "w" else "-") +
		(if (canExecute()) "x" else "-")

private fun File.type(): String = when {
	isFile -> "f"
	isDirectory -> "d"
	exists() -> "e"
	else -> "!"
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
	val task = task(taskPath)
		.let { assertNotNull(it, "${taskPath} task not found"); it!! }
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
