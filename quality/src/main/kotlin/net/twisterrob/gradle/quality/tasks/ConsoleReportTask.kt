package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.ReportLocationsGenerator
import net.twisterrob.gradle.quality.report.ResultGenerator
import net.twisterrob.gradle.quality.report.TableGenerator
import org.gradle.api.tasks.UntrackedTask

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
@UntrackedTask(because = "It is used to inspect state, output is console.")
abstract class ConsoleReportTask : BaseViolationsTask() {

	override fun processViolations(violations: Grouper.Start<Violations>) {
		ResultGenerator().build(violations)?.also(logger::quiet)
		ReportLocationsGenerator().build(violations)?.also(logger::quiet)
		@Suppress("UNCHECKED_CAST")
		val grouped = violations
			.count<Int>()
			.by("module")
			.by("variant")
			.by("parser")
			.group() as Map<String, Map<String, Map<String, Int?>>>
		TableGenerator(
			zeroCount = "." /*TODO ✓*/,
			missingCount = "",
			isPrintEmptyRows = false,
			isPrintEmptyColumns = false
		).build(grouped).also(logger::quiet)
	}
}
