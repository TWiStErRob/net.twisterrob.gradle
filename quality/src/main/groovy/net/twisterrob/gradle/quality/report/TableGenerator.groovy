package net.twisterrob.gradle.quality.report

import net.twisterrob.gradle.common.Utils

import java.util.function.Function
import java.util.stream.Collectors

class TableGenerator {

	public static final int MIN_MODULE_LENGTH = 7 // Summary
	public static final int MIN_VARIANT_LENGTH = 9 // (total: x)
	private String columnSeparator = '\t'
	private String missingCount = 'N/A'
	private String zeroCount = '0'
	private boolean printEmptyColumns = true
	private boolean printEmptyRows = true
	private boolean summaryRow = true
	private int minWidth = 0

	String build(
			Map<String, ? extends Map<String, ? extends Map<String, Integer>>> byModuleByVariantByParserCounts) {
		Collection<String> modules = byModuleByVariantByParserCounts.keySet()
		Collection<String> variants = byModuleByVariantByParserCounts.collectMany {module, byVariantByParserCounts ->
			byVariantByParserCounts.keySet()
		}.unique()
		Collection<String> parsers = byModuleByVariantByParserCounts.collectMany {module, byVariantByParserCounts ->
			byVariantByParserCounts.collectMany {variant, byParserCounts ->
				byParserCounts.keySet()
			}
		}.unique()
		Map<String, Optional<Integer>> summary = parsers
				.stream()
				.collect(Collectors.toMap(Function.<String> identity(), {String parser ->
			Optional.ofNullable(byModuleByVariantByParserCounts
					.values()
					.stream()
					.flatMap({it.values().stream()})
					.collect(Utils.nullSafeSum({it[parser]}))
			)
		} as Function<String, Optional<Integer>>))
		def total = summary.values().sum {Optional<Integer> count -> count.orElse(0)} as int

		if (!printEmptyColumns) {
			parsers = parsers.grep {String parser -> summary[parser]?.isPresent()}
		}
		def format = parsers.collect {Math.max(minWidth, it.length())}.collect {"%${it}s"}.join(columnSeparator)
		def longestModule = modules.max {it.length()}
		def longestVariant = variants.max {it.length()}
		def moduleWidth = Math.max(MIN_MODULE_LENGTH, longestModule.length())
		def variantWidth = Math.max(MIN_VARIANT_LENGTH + Math.log10(total) as int, longestVariant.length())
		def rowHeaderFormat = "%-${moduleWidth}s${columnSeparator}%-${variantWidth}s"
		def rowFormat = "${rowHeaderFormat}${columnSeparator}${format}"
		def header = String.format(rowFormat, [ 'module', 'variant' ] + parsers as Object[])
		def rows = byModuleByVariantByParserCounts.collectMany {module, byVariantByParserCounts ->
			byVariantByParserCounts.collectMany {variant, byParserCounts ->
				if (!printEmptyRows && byParserCounts.values().count {it != null} == 0) {
					return [ ] as List<String>
				}
				def results = parsers.collect { // null -> missingCount, 0 -> zeroCount, x -> x
					byParserCounts[it] == 0? zeroCount : (byParserCounts[it] as String?: missingCount)
				}
				def row = String.format(rowFormat, [ module, variant ] + results as Object[])
				return [ row ]
			}
		}
		List<String> footer = [ ]
		if (summaryRow) {
			def summaryHeader = [ 'Summary', "(total ${total})" as String ]
			List<String> summaryData = parsers.collect {summary[it].map({it.toString()}).orElse(missingCount)}
			def summaryRow = String.format(rowFormat, summaryHeader + summaryData as Object[])
			footer = [ summaryRow ]
		}
		return ([ header ] + rows + footer).join(System.lineSeparator())
	}
}
