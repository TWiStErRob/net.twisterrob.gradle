package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.TableGenerator
import org.gradle.api.tasks.UntrackedTask

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
@UntrackedTask(because = "It is used to inspect state, output is console.")
abstract class ConsoleReportTask : BaseViolationsTask() {

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
			isPrintEmptyRows = false,
			isPrintEmptyColumns = false
		).build(grouped)
		val result = violations
			.list
			.flatMap { it.violations.orEmpty() }
			.map { violation ->
				val message = violation.message.replace("""(\r?\n)+""".toRegex(), System.lineSeparator())
				val loc = violation.location
				@Suppress("detekt.StringShouldBeRawString") // Not using raw string literals because of the new-line requirements.
				return@map (""
						+ "\n${loc.file.absolutePath}:${loc.startLine} in ${loc.module.path}/${loc.variant}"
						+ "\n\t${violation.source.reporter}/${violation.rule}"
						+ "\n${message.prependIndent("\t")}"
						)
			}
		val reportLocations = violations
			.list
			.filter { it.violations.orEmpty().isNotEmpty() }
			.map { "${it.module}:${it.parser}@${it.variant} (${it.violations.orEmpty().size}): ${it.report}" }

		if (result.isNotEmpty()) {
			logger.quiet(
				result.joinToString(
					separator = System.lineSeparator() + System.lineSeparator(),
					postfix = System.lineSeparator()
				)
			)
		}
		if (reportLocations.isNotEmpty()) {
			logger.quiet(
				reportLocations.joinToString(
					separator = System.lineSeparator(),
					postfix = System.lineSeparator()
				)
			)
		}
		logger.quiet(table)
	}
}
