package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations

internal typealias Category = String?
internal typealias Reporter = String

@Suppress("USELESS_CAST") // casts are useful to convert String to typealias
internal fun group(violationss: List<Violations>): Map<Category, Map<Reporter, List<Violation>>> {
	val allViolations = violationss.flatMap { (it.violations ?: emptyList()) }

	@Suppress("UnnecessaryVariable") // REPORT broken refactor: inlining this variable breaks code
	val group = allViolations
		.groupBy { it.category as Category }
		.toSortedMap(nullsLast(compareBy { it }))
		.mapValues { (_, violations) ->
			violations
				.groupBy { it.source.reporter as Reporter }
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
