package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation
import java.net.URI
import java.util.Locale

class DocumentationGenerator {
	/**
	 * @return a [URI], not a [java.net.URL], because URL resolves the DNS.
	 */
	@Suppress("detekt.CognitiveComplexMethod", "detekt.UseLet") // Nicely structured, so it's ok.
	fun getDocumentationUrl(v: Violation): URI? =
		when (v.source.reporter) {
			"ANDROIDLINT" -> {
				val check = v.rule
				URI.create("https://googlesamples.github.io/android-custom-lint-rules/checks/${check}.md.html")
			}
			"CHECKSTYLE" -> {
				if (v.category != null) {
					val check = v.rule.removeSuffix("Check")
					val category = v.category.lowercase(Locale.ROOT)
					URI.create("https://checkstyle.sourceforge.io/config_${category}.html#${check}")
				} else {
					null
				}
			}
			"PMD" -> {
				if (v.category == "Custom") {
					null
				} else {
					val match = PMD_DOC_LINK_LINE.find(v.message)
					if (match != null) {
						val url = match.groupValues[1]
						URI.create(url)
					} else if (v.category != null) {
						val rule = v.rule.lowercase(Locale.ROOT)
						val ruleSet = "java".lowercase(Locale.ROOT)
						val category = v.category.lowercase(Locale.ROOT).replace(" ", "")
						URI.create("https://pmd.github.io/latest/pmd_rules_${ruleSet}_${category}.html#${rule}")
					} else {
						null
					}
				}
			}
			"DETEKT" -> {
				if (v.category != null) {
					val category = v.category.lowercase(Locale.ROOT)
					val rule = v.rule.lowercase(Locale.ROOT)
					URI.create("https://detekt.dev/docs/rules/${category}#${rule}")
				} else {
					null
				}
			}
			else -> {
				// No documentation available, because the reporter is not known.
				null
			}
		}

	companion object {
		internal val PMD_DOC_LINK_LINE: Regex =
			Regex("""^.+ (https://pmd\.github\.io/[^/]+/pmd_rules_.*)$""", RegexOption.MULTILINE)
	}
}
