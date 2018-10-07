package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violation.Location

/**
 * Assumes order by category then by rule then by file then by line then by column.
 */
internal fun collapse(violations: List<Violation>): List<Violation> =
	violations
		.groupBy { it.rule }
		.mapValues { (_, list) -> collapseUniform(list) }
		.flatMap { it.value }

internal fun collapseUniform(violations: List<Violation>): List<Violation> =
	violations
		.groupBy { it.location.file }
		.mapValues { (_, list) -> collapseFile(list) }
		.flatMap { it.value }

internal fun collapseFile(violations: List<Violation>): List<Violation> {
	@Suppress("SimplifyBooleanWithConstants")
	fun verySimilarProblem(v1: Violation, v2: Violation): Boolean =
		true
				&& v1.rule == v2.rule
				&& v1.category == v2.category
				&& v1.severity == v2.severity
				&& v1.specifics == v2.specifics
				//&& v1.message == v2.message
				&& v1.location.module == v2.location.module
				&& v1.location.variant == v2.location.variant
				&& v1.location.file == v2.location.file
				//&& v1.location.startLine == v2.location.startLine
				//&& v1.location.endLine == v2.location.endLine
				//&& v1.location.column == v2.location.column
				&& v1.source.parser == v2.source.parser
				&& v1.source.gatherer == v2.source.gatherer
				&& v1.source.reporter == v2.source.reporter
				&& v1.source.source == v2.source.source
				&& v1.source.report == v2.source.report
				&& v1.source.humanReport == v2.source.humanReport

	fun merge(list: List<Violation>): Violation {
		val first = list.first()
		return Violation(
			first.rule,
			first.category,
			first.severity,
			list.joinToString { it.message },
			first.specifics,
			Location(
				first.location.module,
				first.location.variant,
				first.location.file,
				first.location.startLine,
				list.last().location.endLine,
				first.location.column
			),
			first.source
		)
	}

	var continuation: MutableList<Violation> = mutableListOf(violations.first())
	val mergeds = mutableListOf<Violation>()
	for (next in violations.asSequence().drop(1)) {
		if (verySimilarProblem(next, continuation.last())) {
			if (continuation.last().location.endLine + 1 == next.location.startLine) {
				// found a continuation, save and continue searching
				continuation.add(next)
				continue
			}
		}
		// if reached here, something is different and next is not part of the [first, last] group
		// continuation was already found, merge them (possible that continuation.size == 1, it still works
		mergeds.add(merge(continuation))
		// continue searching from next one
		continuation = mutableListOf(next)
	}
	// merge remainder (i.e. last group); this could be a single input item as well
	mergeds.add(merge(continuation))
	return mergeds
}
