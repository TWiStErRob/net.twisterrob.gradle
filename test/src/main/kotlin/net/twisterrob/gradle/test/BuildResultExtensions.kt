// Named so that Groovy code can consume it as extension methods as well
// (see ExtensionModule in META-INF)
@file:JvmName("BuildResultExtensions")

package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult

fun BuildResult.assertHasOutputLine(expectedLineRegex: Regex) {
	assertHasOutputLine(null, expectedLineRegex)
}

fun BuildResult.assertHasOutputLine(expectedLine: String) {
	assertHasOutputLine(null, expectedLine)
}

fun BuildResult.assertHasOutputLine(reason: String?, expectedLineRegex: Regex) {
	assertRegex(reason, """(?m)^${expectedLineRegex.pattern}$""".toRegex())
}

fun BuildResult.assertHasOutputLine(reason: String?, expectedLine: String) {
	assertRegex(reason, """(?m)^${Regex.escape(expectedLine)}$""".toRegex())
}

fun BuildResult.assertNoOutputLine(unexpectedLineRegex: Regex) {
	assertNoOutputLine(null, unexpectedLineRegex)
}

fun BuildResult.assertNoOutputLine(unexpectedLine: String) {
	assertNoOutputLine(null, unexpectedLine)
}

fun BuildResult.assertNoOutputLine(reason: String?, unexpectedLineRegex: Regex) {
	assertRegex(reason, """(?m)^${unexpectedLineRegex.pattern}$""".toRegex(), false)
}

fun BuildResult.assertNoOutputLine(reason: String?, unexpectedLine: String) {
	assertRegex(reason, """(?m)^${Regex.escape(unexpectedLine)}$""".toRegex(), false)
}

private fun BuildResult.assertRegex(reason: String?, regex: Regex, positive: Boolean = true) {
	assert(positive == regex.containsMatchIn(output), {
		"""
			${reason ?: ""}
			Expected:
			${if (positive) "" else "No match for "}${regex}
			Actual:
${output.prependIndent("\t\t\t")}
		""".trimIndent()
	})
}

val BuildResult.failReason: String?
	get() = findFailureBlock("What went wrong")

val BuildResult.failSuggestion: String?
	get() = findFailureBlock("Try")

val BuildResult.fullException: String?
	get() = findFailureBlock("Exception is")

fun BuildResult.findFailureBlock(label: String): String {
	val fullLabel = "* ${label}:"
	return output
			.split(System.lineSeparator())
			.dropWhile { it != fullLabel }
			.drop(1)
			.takeWhile { !it.startsWith("* ") }
			.joinToString(System.lineSeparator())
			.trim()
}
