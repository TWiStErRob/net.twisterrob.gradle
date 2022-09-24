package net.twisterrob.gradle.quality.report

import net.twisterrob.gradle.common.safeAdd
import kotlin.math.log10

private typealias Module = String
private typealias Variant = String
private typealias Parser = String
private typealias MaybeCount = Int?

@Suppress("LongParameterList") // These are also the constants that would be otherwise in companion, but overridable.
class TableGenerator(
	private val columnSeparator: String = "\t",
	private val missingCount: String = "N/A",
	private val zeroCount: String = "0",
	private val printEmptyColumns: Boolean = true,
	private val printEmptyRows: Boolean = true,
	private val summaryRow: Boolean = true,
	private val minWidth: Int = 0
) {

	@Suppress(
		"SpreadOperator", // Open to suggestions.
		"ReturnCount", // TODEL https://github.com/detekt/detekt/issues/5341
	)
	fun build(byModuleByVariantByParserCounts: Map<Module, Map<Variant, Map<Parser, MaybeCount>>>): String {
		val modules = byModuleByVariantByParserCounts.keys
		val variants = byModuleByVariantByParserCounts.flatMap { it.value.keys }.distinct()
		var parsers = byModuleByVariantByParserCounts.flatMap { it.value.values }.flatMap { it.keys }.distinct()
		val summary: Map<Parser, MaybeCount> = parsers.associateBy({ it }) { parser ->
			byModuleByVariantByParserCounts
				.values
				.asSequence()
				// Note: Kotlin 1.4 introduced Sequence.flatMap(()->Iterable), Gradle <6.8 uses Kotlin 1.3.x
				.flatMap { it.values.asSequence() }
				.map { it[parser] }
				.reduce(::safeAdd)
		}

		if (!printEmptyColumns) {
			parsers = parsers.filter { summary[it] != null }
		}
		val format = parsers.map { it.length.coerceAtLeast(minWidth) }.joinToString("") { "${columnSeparator}%${it}s" }
		val longestModule = modules.maxByOrNull { it.length }
		val longestVariant = variants.maxByOrNull { it.length }
		val moduleWidth = (longestModule?.length ?: 0).coerceAtLeast(MIN_MODULE_LENGTH)
		val total = summary.values.sumBy { it ?: 0 }
		val totalCountWidth = if (total == 0) 1 else log10(total.toDouble()).toInt()
		val variantWidth = (longestVariant?.length ?: 0).coerceAtLeast(MIN_VARIANT_LENGTH + totalCountWidth)
		val rowHeaderFormat = "%-${moduleWidth}s${columnSeparator}%-${variantWidth}s"
		val rowFormat = "${rowHeaderFormat}${format}"
		val header = String.format(rowFormat, *(listOf("module", "variant") + parsers).toTypedArray())
		val rows = byModuleByVariantByParserCounts.flatMap { byModule ->
			byModule.value.flatMap row@{ byVariant ->
				val byParserCounts = byVariant.value
				if (!printEmptyRows && byParserCounts.values.count { it != null } == 0) {
					return@row emptyList<String>()
				}
				val cells = parsers.map cell@{
					return@cell when (byParserCounts[it]) {
						0 -> zeroCount
						null -> missingCount
						else -> byParserCounts[it].toString()
					}
				}
				val row = String.format(rowFormat, *(listOf(byModule.key, byVariant.key) + cells).toTypedArray())
				return@row listOf(row)
			}
		}
		val footer = if (summaryRow) {
			val summaryHeader = listOf("Summary", "(total: ${total})")
			val summaryData = parsers.map { summary[it]?.toString() ?: missingCount }
			val summaryRow = String.format(rowFormat, *(summaryHeader + summaryData).toTypedArray())
			listOf(summaryRow)
		} else {
			emptyList()
		}
		return (listOf(header) + rows + footer).joinToString(System.lineSeparator())
	}

	companion object {
		private const val MIN_MODULE_LENGTH: Int = 7 // Summary
		private const val MIN_VARIANT_LENGTH: Int = 9 // (total: x)
	}
}
