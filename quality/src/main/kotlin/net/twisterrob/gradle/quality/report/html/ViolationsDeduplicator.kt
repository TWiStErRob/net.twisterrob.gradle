package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations

private typealias Module = String
private typealias Variant = String

/**
 * Remove duplicate violations.
 * Duplicates are: in the same location and the same problem, see [Deduper].
 * Approach: get violations that affect all variants and remove them from variant-specific violations
 */
@Suppress("USELESS_CAST")
fun deduplicate(violations: List<Violations>): List<Violations> {
	return violations
		.groupBy { it.module as Module }
		.mapValues { (_, list) ->
			val byVariant = list.groupBy { it.variant as Variant }
			val all = byVariant[ALL_VARIANTS_NAME] ?: return@mapValues list
			val filtered = byVariant.filterKeys { it != ALL_VARIANTS_NAME }
			val deduplicated = filtered.flatMap { (_, violations) ->
				return@flatMap violations.map { removeDuplicates(from = it, using = all) }
			}
			return@mapValues all + deduplicated
		}
		.values
		.flatten()
}

private fun removeDuplicates(from: Violations, using: List<Violations>): Violations =
	using.fold(from) { reduced, next -> removeDuplicates(reduced, next) }

private fun removeDuplicates(from: Violations, using: Violations): Violations {
	return Violations(
		parser = from.parser,
		module = from.module,
		variant = from.variant,
		result = from.result,
		report = from.report,
		violations = removeOptionalDuplicates(from.violations, using.violations)
	)
}

private fun removeOptionalDuplicates(from: List<Violation>?, using: List<Violation>?): List<Violation>? {
	if (from == null || using == null) return null
	return removeDuplicates(from, using).ifEmpty { null }
}

private fun removeDuplicates(from: List<Violation>, using: List<Violation>): List<Violation> {
	val set = mutableSetOf<Deduper>().apply { addAll(from.map { Deduper(it) }) }
	using.forEach { set.remove(Deduper(it)) }
	return set.map { it.violation }
}

private class Deduper(val violation: Violation) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Deduper) return false

		if (violation.rule != other.violation.rule) return false
		if (violation.category != other.violation.category) return false
		if (violation.message != other.violation.message) return false
		if (violation.location.module != other.violation.location.module) return false
		if (violation.location.file != other.violation.location.file) return false
		if (violation.location.startLine != other.violation.location.startLine) return false
		if (violation.location.endLine != other.violation.location.endLine) return false
		if (violation.location.column != other.violation.location.column) return false

		return true
	}

	override fun hashCode(): Int {
		var result = violation.rule.hashCode()
		result = 31 * result + violation.message.hashCode()
		result = 31 * result + (violation.category?.hashCode() ?: 0)
		result = 31 * result + violation.location.module.hashCode()
		result = 31 * result + violation.location.file.hashCode()
		result = 31 * result + violation.location.startLine.hashCode()
		result = 31 * result + violation.location.endLine.hashCode()
		result = 31 * result + violation.location.column.hashCode()
		return result
	}
}
