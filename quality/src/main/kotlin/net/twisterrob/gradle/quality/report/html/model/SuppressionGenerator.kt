package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation

class SuppressionGenerator {
	@Suppress("CyclomaticComplexMethod") // Nicely structured, so it's ok.
	fun getSuppression(v: Violation): String? =
		when (v.source.reporter) {
			"ANDROIDLINT" -> {
				when (v.location.file.extension) {
					"java" -> """@SuppressLint("${v.rule}") // TODO explanation"""
					"kt" -> """@SuppressLint("${v.rule}") // TODO explanation"""
					"xml" -> """tools:ignore="${v.rule}""""
					"gradle" -> """//noinspection ${v.rule} TODO explanation"""
					else -> """
						<issue id="${v.rule}" severity="ignore">
						    <!-- TODO explanation -->
						    <ignore path="${if (v.isLocationExternal) v.location.file.name else v.locationRelativeToModule}" />
						</issue>
					""".trimIndent()
				}
			}

			"CHECKSTYLE" ->
				when (v.location.file.extension) {
					"java" -> """@SuppressWarnings("checkstyle:${v.rule}") // TODO explanation"""
					"kt" -> """@Suppress("checkstyle:${v.rule}") // TODO explanation"""
					else -> null
				}
			"PMD" ->
				when (v.location.file.extension) {
					"java" -> """@SuppressWarnings("PMD.${v.rule}") // TODO explanation"""
					"kt" -> """@Suppress("PMD.${v.rule}") // TODO explanation"""
					else -> null
				}

			else -> null
		}
}

private val Violation.isLocationExternal: Boolean
	get() = LocationViewModel(this).isLocationExternal

private val Violation.locationRelativeToModule: String
	get() = LocationViewModel(this).locationRelativeToModule
