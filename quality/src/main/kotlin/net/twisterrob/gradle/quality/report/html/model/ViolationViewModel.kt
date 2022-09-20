package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation

@Suppress("UseDataClass") // ViewModel pattern, no need for data class methods.
class ViolationViewModel(
	val location: LocationViewModel,
	val source: SourceViewModel,
	val details: DetailsViewModel,
	val specifics: Map<String, String>
) {

	companion object {
		fun create(violation: Violation): ViolationViewModel =
			ViolationViewModel(
				LocationViewModel(violation),
				SourceViewModel(violation),
				DetailsViewModel(violation),
				violation.specifics
			)
	}
}
