package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation

class DetailsViewModel(private val v: Violation) {
	val rule: String get() = v.rule
	val suppression: String? get() = getSuppression(v)
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
					fun String.escapeMarkdownForJSTemplate(): String = this
						.replace("""\""", """\\""")
						.replace("""$""", """\$""")
						.replace("""`""", """\`""")
						.replace("""&#xA;""", "\n")

					val lines = v.message.lineSequence()
					title = lines.elementAt(0)
					message = run {
						val messageLine = lines.elementAt(1)
						cleanLintMessage(v.rule, messageLine).escapeMarkdownForJSTemplate()
					}
					description = run {
						lines
							.drop(2) // already used 0 and 1 above
							.joinToString("\n")
							.escapeMarkdownForJSTemplate()
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

private fun cleanLintMessage(check: String, messageLine: String): String = when (check) {
	"IconMissingDensityFolder" ->
		messageLine.replace(Regex("""(?<=Missing density variation folders in `)(.*?)(?=`:)""")) {
			it.value.replace("""\\""", """\""")
		}

	else -> messageLine
}

private fun getSuppression(v: Violation): String? =
	when (v.source.reporter) {
		"ANDROIDLINT" -> {
			when (v.location.file.extension) {
				"java" -> """@SuppressLint("${v.rule}") // TODO explanation"""
				"kt" -> """@SuppressLint("${v.rule}") // TODO explanation"""
				"xml" -> """tools:ignore="${v.rule}""""
				"gradle" -> """//noinspection ${v.rule} TODO explanation"""
				else -> """
					|<issue id="${v.rule}" severity="ignore">
					|    <!-- TODO explanation -->
					|    <ignore path="${if (v.isLocationExternal) v.location.file.name else v.locationRelativeToModule}" />
					|</issue>
				""".trimMargin()
			}
		}

		else -> null
	}

private val Violation.isLocationExternal: Boolean
	get() = LocationViewModel(this).isLocationExternal

private val Violation.locationRelativeToModule: String
	get() = LocationViewModel(this).locationRelativeToModule
