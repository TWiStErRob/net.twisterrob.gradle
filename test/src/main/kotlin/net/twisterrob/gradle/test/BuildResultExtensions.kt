// Named so that Groovy code can consume it as extension methods as well
// (see ExtensionModule in META-INF)
@file:JvmName("BuildResultExtensions")

package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.junit.Assert.assertTrue

/**
 * Matches a full line containing the regex.
 * [expectedLineRegex] needs to include `.*` if the line beginning/end doesn't matter.
 */
fun BuildResult.assertHasOutputLine(expectedLineRegex: Regex) {
	assertHasOutputLine(null, expectedLineRegex)
}

/**
 * Matches a full line containing the text. An exact match will be performed against [expectedLine].
 */
fun BuildResult.assertHasOutputLine(expectedLine: String) {
	assertHasOutputLine(null, expectedLine)
}

fun BuildResult.assertHasOutputLine(reason: String?, expectedLineRegex: Regex) {
	assertRegex(reason, """(?m)^${expectedLineRegex.pattern}$""".toRegex())
}

fun BuildResult.assertHasOutputLine(reason: String?, expectedLine: String) {
	assertRegex(reason, """(?m)^${Regex.escape(expectedLine)}$""".toRegex())
}

/**
 * Matches a full line not containing the regex.
 * [expectedLineRegex] needs to include `.*` if the line beginning/end doesn't matter.
 */
fun BuildResult.assertNoOutputLine(unexpectedLineRegex: Regex) {
	assertNoOutputLine(null, unexpectedLineRegex)
}

/**
 * Matches a full line containing the text. An exact match will be performed against [expectedLine].
 * Use [#assertNoOutputLine(Regex)] with heading/trailing `.*` if parts don't matter.
 */
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
	val message = """
		${reason ?: ""}
		Expected:
		${if (positive) "" else "No match for "}${regex}
		Actual:
${output.prependIndent("\t\t")}
	""".trimIndent()
	assertTrue(message, positive == regex.containsMatchIn(output))
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
