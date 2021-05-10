package net.twisterrob.gradle.quality.report

import net.twisterrob.gradle.common.safeAdd

private typealias Module = String
private typealias Variant = String
private typealias Parser = String
private typealias MaybeCount = Int?

class TableGenerator(
	private val columnSeparator: String = "\t",
	private val missingCount: String = "N/A",
	private val zeroCount: String = "0",
	private val printEmptyColumns: Boolean = true,
	private val printEmptyRows: Boolean = true,
	private val summaryRow: Boolean = true,
	private val minWidth: Int = 0
) {

	companion object {
		const val MIN_MODULE_LENGTH = 7 // Summary
		const val MIN_VARIANT_LENGTH = 9 // (total: x)
	}

	fun build(byModuleByVariantByParserCounts: Map<Module, Map<Variant, Map<Parser, MaybeCount>>>): String {
		val modules = byModuleByVariantByParserCounts.keys
		val variants = byModuleByVariantByParserCounts.flatMap { it.value.keys }.distinct()
		var parsers = byModuleByVariantByParserCounts.flatMap { it.value.values }.flatMap { it.keys }.distinct()
		val summary: Map<Parser, MaybeCount> = parsers.associateBy({ it }) { parser ->
			byModuleByVariantByParserCounts
				.values
				.flatMap({ it.values })
				.map({ it[parser] })
				.reduce(::safeAdd)
		}

		if (!printEmptyColumns) {
			parsers = parsers.filter { summary[it] != null }
		}
		val format = parsers.map { Math.max(minWidth, it.length) }.joinToString("") { "${columnSeparator}%${it}s" }
		val longestModule = modules.maxBy { it.length }
		val longestVariant = variants.maxBy { it.length }
		val moduleWidth = Math.max(MIN_MODULE_LENGTH, longestModule?.length ?: 0)
		val total = summary.values.sumBy { it ?: 0 }
		val totalCountWidth = if (total == 0) 1 else Math.log10(total.toDouble()).toInt()
		val variantWidth = Math.max(MIN_VARIANT_LENGTH + totalCountWidth, longestVariant?.length ?: 0)
		val rowHeaderFormat = "%-${moduleWidth}s${columnSeparator}%-${variantWidth}s"
		val rowFormat = "${rowHeaderFormat}${format}"
		val header = String.format(rowFormat, *(listOf("module", "variant") + parsers).toTypedArray())
		val rows = byModuleByVariantByParserCounts.flatMap { byModule ->
			byModule.value.flatMap row@{ byVariant ->
				val byParserCounts = byVariant.value
				if (!printEmptyRows && byParserCounts.values.count { it != null } == 0) {
					return@row listOf<String>()
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
			listOf()
		}
		return (listOf(header) + rows + footer).joinToString(System.lineSeparator())
	}
}
