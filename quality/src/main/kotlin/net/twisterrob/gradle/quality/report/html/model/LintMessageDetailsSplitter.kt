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

		private val specialCases: Map<String, (Violation) -> MessageDetails> = mapOf(
			"IconMissingDensityFolder" to fun(v: Violation): MessageDetails {
				val split = defaultSplit(v)
				val replaced = split.message
					.replace(Regex("""(?<=Missing density variation folders in `)(.*?)(?=`:)""")) {
						it.value.replace("""\\""", """\""")
					}
				return split.copy(message = replaced)
			},
			// Consider using ContextViewModel.ErrorContext for displaying the stack trace of this.
			"LintError" to fun(v: Violation): MessageDetails {
				val split = defaultSplit(v)
				val replaced =
					if ("←" in split.message) {
						split.message
							.replace(Regex("""Stack: `(.*?):"""), "$0←")
							.replace(
								Regex(
									"""
										Unexpected failure during lint analysis of (.*?) \(this is a bug in lint or one of the libraries it depends on\)
										
										Stack: `(.*?):(.*)`\n\nYou can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout\.
									""".trimIndent()
								)
							) { match ->
								"""
									Unexpected failure during lint analysis of `${match.groupValues[1]}`.
									
									```
									Exception in thread "lint" ${match.groupValues[2]}:${
										match.groupValues[@Suppress("MagicNumber") 3]
											.replace("←", "\n\tat ")
											.prependIndent("\t\t\t\t\t\t\t\t\t")
											.trimStart('\t')
									}
									```
								""".trimIndent()
							}
					} else {
						split.message
							.replace(
								Regex(
									"""
										Unexpected failure during lint analysis of (.*?) \(this is a bug in lint or one of the libraries it depends on\)
										
										Stack: `(.*?):`\n\nYou can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout\.
									""".trimIndent()
								)
							) { match ->
								"""
									`${match.groupValues[2]}` during lint analysis of `${match.groupValues[1]}`.
								""".trimIndent()
							}
					}
				return split.copy(
					message = replaced,
					description = """
						This is a bug in lint or one of the libraries it depends on.
						
						You can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout.
						
						
					""".trimIndent() + split.description
				)
			}
		)

		private fun String.replaceNewLines(): String =
			replace("""&#xA;""", "\n")

		/**
		 * Reverse of [se.bjurr.violations.lib.parsers.AndroidLintParser.parseReportOutput].
		 */
		private fun defaultSplit(v: Violation): MessageDetails {
			val lines = v.message.lineSequence()

			return MessageDetails(
				title = lines.elementAt(0).replaceNewLines(),
				message = lines.elementAt(1).replaceNewLines(),
				description = lines
					.drop(2) // already used 0 and 1 above
					.joinToString("\n")
					.replaceNewLines()
			)
		}
	}
}
