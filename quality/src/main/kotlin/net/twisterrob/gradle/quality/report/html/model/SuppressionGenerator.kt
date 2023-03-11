package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation

class SuppressionGenerator {
	@Suppress("CyclomaticComplexMethod") // Nicely structured, so it's ok.
	fun getSuppression(v: Violation): String? =
		when (v.source.reporter) {
			"ANDROIDLINT" -> {
				when (v.location.file.extension) {
					"java" -> """@SuppressLint("${v.rule}") // TODO Explanation."""
					"kt" -> """@SuppressLint("${v.rule}") // TODO Explanation."""
					"kts" -> """@Suppress("${v.rule}") // TODO Explanation."""
					"xml" -> """tools:ignore="${v.rule}""""
					"gradle" -> """//noinspection ${v.rule} TODO Explanation."""
					else -> """
						<issue id="${v.rule}">
						    <!-- TODO Explanation. -->
						    <ignore path="${v.ignorePath}" />
						</issue>
					""".trimIndent()
				}
			}

			"CHECKSTYLE" ->
				when (v.location.file.extension) {
					"java" -> """@SuppressWarnings("checkstyle:${v.rule}") // TODO Explanation."""
					"kt" -> """@Suppress("checkstyle:${v.rule}") // TODO Explanation."""
					else -> null
				}
			"PMD" ->
				when (v.location.file.extension) {
					"java" -> """@SuppressWarnings("PMD.${v.rule}") // TODO Explanation."""
					"kt" -> """@Suppress("PMD.${v.rule}") // TODO Explanation."""
					else -> null
				}

			else -> null
		}
}

private val Violation.ignorePath: String
	get() {
		val vm = LocationViewModel(this)
		return when {
			vm.isLocationExternal -> vm.fileName
			this.location.file.isDirectory -> vm.locationRelativeToModule.replace("\\", "/") + vm.fileName
			else -> vm.locationRelativeToModule.replace("\\", "/") + vm.fileName
		}
	}
