package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation

class ViolationViewModel(
	val location: LocationViewModel,
	val source: SourceViewModel,
	val details: DetailsViewModel,
	val specifics: Map<String, String>
) {

	companion object {
		fun create(violation: Violation) = ViolationViewModel(
			LocationViewModel(violation),
			SourceViewModel(violation),
			DetailsViewModel(violation),
			violation.specifics
		)
	}
}
