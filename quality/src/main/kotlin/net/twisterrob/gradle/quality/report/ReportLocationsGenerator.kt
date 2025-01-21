package net.twisterrob.gradle.quality.report

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import kotlin.collections.isNotEmpty
import kotlin.collections.orEmpty

internal class ReportLocationsGenerator {

	fun build(violations: Grouper.Start<Violations>): String? {
		val reportLocations = violations
			.list
			.filter { it.violations.orEmpty().isNotEmpty() }
			.map { "${it.module}:${it.parser}@${it.variant} (${it.violations.orEmpty().size}): ${it.report}" }
		return if (reportLocations.isNotEmpty()) {
			reportLocations.joinToString(
				separator = System.lineSeparator(),
				postfix = System.lineSeparator()
			)
		} else {
			null
		}
	}
}
