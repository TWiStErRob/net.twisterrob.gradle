package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.common.grouper.Grouper.Start
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations

internal fun group(results: Start<Violations>): Map<String?, Map<String, List<Violation>>> {
	val allViolations = results.list.flatMap { (it.violations ?: emptyList()) }

	// REPORT inlining this variable breaks code
	val group = allViolations
		.groupBy { it.category }
		.toSortedMap(nullsLast(compareBy { it }))
		.mapValues { (_, violations) ->
			violations
				.groupBy { it.source.reporter }
				.toSortedMap(compareBy { it })
				.mapValues { (_, violations) ->
					violations.sortedWith(
						compareBy<Violation> { it.rule }
							.thenBy { it.location.file }
							.thenBy { it.location.startLine }
							.thenBy { it.location.column }
					)
				}
		}
	return group
}
