package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violation.Source

@Suppress("detekt.UseDataClass") // TODEL https://github.com/detekt/detekt/issues/5339
class SourceViewModel(violation: Violation) {
	private val s: Source = violation.source
	val parser: String = s.parser
	val source: String = s.source
	val reporter: String = s.reporter
}
