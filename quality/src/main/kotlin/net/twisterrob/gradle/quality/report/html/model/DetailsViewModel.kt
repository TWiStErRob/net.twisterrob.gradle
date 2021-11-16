package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation

class DetailsViewModel(private val v: Violation) {
	val rule: String get() = v.rule
	val suppression: String? get() = SuppressionGenerator().getSuppression(v)
	val category: String get() = v.category ?: "unknown"
	val severity: String get() = v.severity.toString()
	val messaging: MessagingViewModel by lazy { MessagingViewModel() }
	val context: ContextViewModel by lazy { ContextViewModel.create(v) }

	inner class MessagingViewModel {
		var title: String? = null; private set
		var message: String? = null; private set
		var description: String? = null; private set

		init {
			when (v.source.reporter) {
				"ANDROIDLINT" -> {
					fun String.escapeMarkdownForJSTemplate(): String =
						this
							.replace("""\""", """\\""")
							.replace("""$""", """\$""")
							.replace("""`""", """\`""")

					fun String.preprocessLintMarkdown(): String =
						this
							// https://github.com/TWiStErRob/net.twisterrob.gradle/issues/65#issuecomment-860275509
							.replace("""&""", """&amp;""")

					val details = LintMessageDetailsSplitter().split(v)
					title = details.title.preprocessLintMarkdown().escapeMarkdownForJSTemplate()
					message = details.message.preprocessLintMarkdown().escapeMarkdownForJSTemplate()
					description = details.description.preprocessLintMarkdown().escapeMarkdownForJSTemplate()
				}

				"PMD" -> {
					val lines = v.message.lineSequence()
					message = lines.elementAt(0)
					description = run {
						lines
							.drop(1) // already used 0 above
							.joinToString("\n")
					}
				}

				else -> {
					if (v.message.count { it == '\n' } >= 1) {
						description = v.message
					} else {
						message = v.message
					}
				}
			}
		}
	}
}
