// Named so that Groovy code can consume it as extension methods as well
// (see ExtensionModule in META-INF)
@file:JvmName("BuildResultExtensions")

package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult

fun BuildResult.assertHasOutputLine(expectedLineRegex: Regex) {
	assertRegex("""(?m)^${expectedLineRegex.pattern}$""".toRegex())
}

fun BuildResult.assertHasOutputLine(expectedLineRegex: String) {
	assertRegex("""(?m)^${Regex.escape(expectedLineRegex)}$""".toRegex())
}

private fun BuildResult.assertRegex(regex: Regex) {
	assert(regex.containsMatchIn(output), {
		"""
			Expected:
			${regex}
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
