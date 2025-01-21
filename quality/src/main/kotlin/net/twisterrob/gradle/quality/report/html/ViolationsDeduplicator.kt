@file:Suppress("detekt.TooManyFunctions") // This defines a whole module in one file.

package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations

private typealias Module = String
private typealias Variant = String
private typealias Parser = String

/**
 * Remove duplicate violations.
 * Duplicates are: in the same location and the same problem, see [Deduper].
 * Approach: get violations that affect all variants and remove them from variant-specific violations
 */
fun deduplicate(violations: List<Violations>): List<Violations> {
	@Suppress("USELESS_CAST") // Make sure chains use the typealias.
	return violations
		.groupBy { it.module as Module }
		.mapValues { (_, list) -> process(mergeIntersections(list)) }
		.values
		.flatten()
}

private fun process(violations: List<Violations>): List<Violations> {
	@Suppress("USELESS_CAST") // Make sure chains use the typealias.
	val byVariant = violations.groupBy { it.variant as Variant }
	val all = byVariant[ALL_VARIANTS_NAME] ?: return violations
	val filtered = byVariant.filterKeys { it != ALL_VARIANTS_NAME }
	val deduplicated = filtered.flatMap { (_, violations) ->
		violations.map { removeDuplicates(from = it, using = all) }
	}
	return all + deduplicated
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
	if (from == null) return null // Nothing to remove from, identity.
	if (using == null) return from // No duplicates to remove, keep everything.
	return removeDuplicates(from, using)
}

@Suppress("ConvertArgumentToSet")
private fun removeDuplicates(from: List<Violation>, using: List<Violation>): List<Violation> {
	val set = from.map { Deduper(it) }.toMutableSet()
	set.removeAll(using.map { Deduper(it) })
	return set.map { it.violation }
}

/**
 * This step will duplicate some violations in variant and "all", but expecting [process] to remove those.
 */
private fun mergeIntersections(violations: List<Violations>): List<Violations> =
	violations
		.groupBy { it.parser.rewrite() }
		.flatMap { (_, list) -> mergeIntersectionsForParser(list) }

@Suppress("detekt.ReturnCount") // Open to suggestions.
private fun mergeIntersectionsForParser(violations: List<Violations>): List<Violations> {
	@Suppress("USELESS_CAST") // Make sure chains use the typealiases.
	val byVariant = violations.groupBy { it.variant as Variant }
	val filtered = byVariant.filterKeys { it != ALL_VARIANTS_NAME }
	if (filtered.size < 2) {
		// Only one variant, don't even try to introduce "all" variants. Also, might be no variants at all.
		return violations
	}
	val intersection = filtered.values.map { it.violations }.intersect()
	if (intersection.isEmpty()) {
		// No common problems, leave everything as it was.
		return violations
	}
	val all = byVariant[ALL_VARIANTS_NAME]?.singleOrNull()
	if (all != null) {
		val newAll = Violations(
			parser = all.parser,
			module = all.module,
			variant = all.variant,
			result = all.result,
			report = all.report,
			violations = all.violations + removeOptionalDuplicates(intersection, all.violations),
		)
		return listOf(newAll) + (violations - all)
	} else {
		// Create new * variant with all the common violations.
		val representative = violations.first()
		val newAll = Violations(
			parser = representative.parser.rewrite(),
			module = representative.module,
			variant = ALL_VARIANTS_NAME,
			// Technically result and report may contain more than just intersection,
			// but they definitely contain the intersection, so it's better than any fake value.
			result = representative.result,
			report = representative.report,
			violations = intersection,
		)
		return listOf(newAll) + violations
	}
}

private val Iterable<Violations>.violations: List<Violation>
	get() = this.flatMap { it.violations.orEmpty() }

private fun Iterable<List<Violation>>.intersect(): List<Violation> =
	this.reduce { acc, next -> intersect(acc, next) }

@Suppress("ConvertArgumentToSet")
private fun intersect(list1: List<Violation>, list2: List<Violation>): List<Violation> {
	val list1D = list1.map { Deduper(it) }
	val list2D = list2.map { Deduper(it) }
	val intersection = list1D intersect list2D
	return intersection.map { it.violation }
}

@Suppress("detekt.UseDataClass") // External equals/hashCode for deduplication.
private class Deduper(val violation: Violation) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Deduper) return false

		if (violation.rule != other.violation.rule) return false
		if (violation.category != other.violation.category) return false
		if (violation.message != other.violation.message) return false
		if (violation.location.module.path != other.violation.location.module.path) return false
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
		result = 31 * result + violation.location.module.path.hashCode()
		result = 31 * result + violation.location.file.hashCode()
		result = 31 * result + violation.location.startLine.hashCode()
		result = 31 * result + violation.location.endLine.hashCode()
		result = 31 * result + violation.location.column.hashCode()
		return result
	}
}

private fun Parser.rewrite(): Parser =
	if (this == "lintVariant") "lint" else this

/**
 * Required bridge to mirror [kotlin.collections.plus] because overload resolution would match the nullable one.
 */
@JvmName("plusOriginal")
private operator fun <T> List<T>.plus(other: List<T>): List<T> =
	(this as Collection<T>).plus(other)

private operator fun <T> List<T>?.plus(other: List<T>?): List<T>? =
	when {
		this == null && other == null -> null
		this == null -> other
		other == null -> this
		else -> this + other
	}
