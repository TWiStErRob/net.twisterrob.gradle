package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import org.gradle.testkit.runner.TaskOutcome.SKIPPED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runners.model.Statement
import java.io.File
import java.util.Date

val GradleRunnerRule.root: File
	get() = this.settingsFile.parentFile!!

fun GradleRunnerRule.delete(path: String) {
	val file = root.resolve(path)
	assertTrue(
		file.deleteRecursively(),
		"Cannot delete $path\n${file.describe()}"
	)
}

fun GradleRunnerRule.move(from: String, to: String) {
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

private fun File.describe(): String =
	absolutePath +
			", stat=${type() + chmod()}" +
			", size=${length()}" +
			", date=${Date(this.lastModified())}"

private fun File.chmod(): String =
	buildString {
		append(if (canRead()) "r" else "-")
		append(if (canWrite()) "w" else "-")
		append(if (canExecute()) "x" else "-")
	}

private fun File.type(): String =
	when {
		isFile -> "f"
		isDirectory -> "d"
		exists() -> "e"
		else -> "!"
	}

fun BuildResult.assertNoTask(taskPath: String) {
	assertNull(task(taskPath))
}

/**
 * Assert that the task exists and that it ran to completion with success.
 * Note: this means that [TaskOutcome.UP_TO_DATE] and [TaskOutcome.NO_SOURCE] is not "success"!
 */
fun BuildResult.assertSuccess(taskPath: String) {
	assertOutcome(taskPath, SUCCESS)
}

fun BuildResult.assertFailed(taskPath: String) {
	assertOutcome(taskPath, FAILED)
}

fun BuildResult.assertSkipped(taskPath: String) {
	assertOutcome(taskPath, SKIPPED)
}

fun BuildResult.assertUpToDate(taskPath: String) {
	assertOutcome(taskPath, UP_TO_DATE)
}

fun BuildResult.assertFromCache(taskPath: String) {
	assertOutcome(taskPath, FROM_CACHE)
}

fun BuildResult.assertNoSource(taskPath: String) {
	assertOutcome(taskPath, NO_SOURCE)
}

fun BuildResult.assertOutcome(taskPath: String, outcome: TaskOutcome) {
	val task = task(taskPath)
		.let { assertNotNull(it, "${taskPath} task not found"); it!! }
	assertEquals(outcome, task.outcome)
}

/**
 * Helper to allow SAM-like behavior for [Statement] abstract class.
 */
@Suppress("TestFunctionName")
inline fun Statement(crossinline block: () -> Unit): Statement =
	object : Statement() {
		override fun evaluate() {
			block()
		}
	}

/**
 * See unit tests for examples.
 */
fun tasksIn(modules: Array<String>, vararg taskNames: String): Array<String> =
	modules
		.flatMap { moduleName ->
			require(moduleName != "") { "Invalid module" }
			val separator = if (moduleName == ":") "" else ":"
			taskNames.map { taskName ->
				require(taskName != "") { "Invalid task" }
				"${moduleName}${separator}${taskName}"
			}
		}
		.toTypedArray()

inline operator fun <reified T> Array<T>.minus(others: Array<T>): Array<T> =
	(this.toList() - @Suppress("ConvertArgumentToSet") others).toTypedArray()
