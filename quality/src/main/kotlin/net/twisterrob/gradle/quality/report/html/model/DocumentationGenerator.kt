package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation
import java.net.URI
import java.util.Locale

class DocumentationGenerator {
	/**
	 * @return a [URI], not a [java.net.URL], because URL resolves the DNS.
	 */
	fun getDocumentationUrl(v: Violation): URI? =
		when (v.source.reporter) {
			"ANDROIDLINT" -> {
				val check = v.rule
				URI.create("https://googlesamples.github.io/android-custom-lint-rules/checks/${check}.md.html")
			}
			"CHECKSTYLE" -> {
				val check = v.rule.removeSuffix("Check")
				val category = v.category!!.toLowerCase(Locale.ROOT)
				URI.create("https://checkstyle.sourceforge.io/config_${category}.html#${check}")
			}
			"PMD" -> {
				val rule = v.rule.toLowerCase(Locale.ROOT).toLowerCase(Locale.ROOT)
				val ruleSet = "java".toLowerCase(Locale.ROOT)
				val category = v.category!!.toLowerCase(Locale.ROOT)
				URI.create("https://pmd.github.io/latest/pmd_rules_${ruleSet}_${category}.html#${rule}")
			}
			"DETEKT" -> {
				val category = v.category!!.toLowerCase(Locale.ROOT)
				val rule = v.rule.toLowerCase(Locale.ROOT)
				URI.create("https://detekt.dev/docs/rules/${category}/#${rule}")
			}
			else -> null
		}
}
