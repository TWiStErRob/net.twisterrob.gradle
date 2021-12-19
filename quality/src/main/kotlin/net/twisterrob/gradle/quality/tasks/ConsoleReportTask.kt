package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.TableGenerator

open class ConsoleReportTask : BaseViolationsTask() {

	override fun processViolations(violations: Grouper.Start<Violations>) {
		@Suppress("UNCHECKED_CAST")
		val grouped = violations
			.count<Int>()
			.by("module")
			.by("variant")
			.by("parser")
			.group() as Map<String, Map<String, Map<String, Int?>>>
		val table = TableGenerator(
			zeroCount = "." /*TODO ✓*/,
			missingCount = "",
			printEmptyRows = false,
			printEmptyColumns = false
		).build(grouped)
		val result = violations
			.list
			.flatMap { it.violations ?: emptyList() }
			.map { violation ->
				val message = violation.message.replace("""(\r?\n)+""".toRegex(), System.lineSeparator())
				val loc = violation.location
				return@map (""
						+ "\n${loc.file.absolutePath}:${loc.startLine} in ${loc.module}/${loc.variant}"
						+ "\n\t${violation.source.reporter}/${violation.rule}"
						+ "\n${message.prependIndent("\t")}"
						)
			}
		val reportLocations = violations
			.list
			.filter { (it.violations ?: emptyList()).isNotEmpty() }
			.map { "${it.module}:${it.parser}@${it.variant} (${it.violations!!.size}): ${it.report}" }

		if (result.isNotEmpty()) {
			println(result.joinToString(System.lineSeparator() + System.lineSeparator()))
			println()
		}
		if (reportLocations.isNotEmpty()) {
			println(reportLocations.joinToString(System.lineSeparator()))
			println()
		}
		if (table.isNotBlank()) {
			println(table)
		}
	}
}
