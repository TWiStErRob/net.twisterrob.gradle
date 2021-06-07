package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation

class LintMessageDetailsSplitter {

	data class MessageDetails(
		val title: String,
		val message: String,
		val description: String
	)

	fun split(v: Violation): MessageDetails {
		val handler = specialCases[v.rule] ?: ::defaultSplit
		return handler(v)
	}

	companion object {

		/**
		 * Reverse of [se.bjurr.violations.lib.parsers.AndroidLintParser.parseReportOutput].
		 */
		private fun defaultSplit(v: Violation): MessageDetails {
			val lines = v.message.lineSequence()
			return MessageDetails(
				title = lines.elementAt(0),
				message = lines.elementAt(1),
				description = lines
					.drop(2) // already used 0 and 1 above
					.joinToString("\n")
			)
		}

		private val specialCases: Map<String, (Violation) -> MessageDetails> = mapOf(
			"IconMissingDensityFolder" to fun(v: Violation): MessageDetails {
				val split = defaultSplit(v)
				val replaced = split.message
					.replace(Regex("""(?<=Missing density variation folders in `)(.*?)(?=`:)""")) {
						it.value.replace("""\\""", """\""")
					}
				return split.copy(message = replaced)
			},
			"LintError" to fun(v: Violation): MessageDetails {
				val split = defaultSplit(v)
				val replaced = split.message
					.replace(
						Regex("""during lint analysis of (.*?) \(this is a bug in lint"""),
						"""during lint analysis of `$1` (this is a bug in lint"""
					)
					.replace(
						Regex("""Stack: `(.*?):"""),
						"""
						```
						Exception in thread "lint" $1:
							at 
						""".trimIndent()
					)
					.replace(
						"""←""",
						"\n\tat "
					)
					.replace(
						Regex("""`&#xA;&#xA;You can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout\."""),
						"\n```"
					)
				return split.copy(
					message = replaced,
					description = "You can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout.\n\n" + split.description
				)
			}
		)
	}
}
