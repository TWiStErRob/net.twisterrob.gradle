package net.twisterrob.gradle.quality.violations

import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser

class RuleCategoryParser {

	fun rule(it: Violation): String =
		@Suppress("UseIfInsteadOfWhen") // Preparing for future parsers.
		when (it.reporter) {
			Parser.CHECKSTYLE.name -> {
				val match = CHECKSTYLE_BUILT_IN_CHECK.matchEntire(it.rule)
				if (match != null) {
					match
						.groupValues[2] // class name
						// Clean redundancy, they don't use Check suffixes either.
						.removeSuffix("Check")
				} else {
					it.rule
						.substringAfterLast(".") // class name
						// Assume name consistent with built-ins.
						.removeSuffix("Check")
				}
			}
			else ->
				it.rule
		}

	fun category(it: Violation): String? =
		when (it.reporter) {
			Parser.CHECKSTYLE.name -> {
				val match = CHECKSTYLE_BUILT_IN_CHECK.matchEntire(it.rule)
				if (match != null) {
					(match.groups[1]?.value ?: "misc").capitalize()
				} else {
					"Custom"
				}
			}
			Parser.PMD.name ->
				@Suppress("UseIfInsteadOfWhen") // Preparing for future category exceptions.
				when (it.category) {
					"Import Statements" -> "Imports"
					else -> it.category
				}
			else ->
				it.category
		}

	companion object {

		private val CHECKSTYLE_BUILT_IN_CHECK: Regex =
			Regex("""^com\.puppycrawl\.tools\.checkstyle\.checks(?:\.([a-z].+))?\.([A-Z].+)$""")
	}
}
