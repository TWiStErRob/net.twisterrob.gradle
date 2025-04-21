package net.twisterrob.gradle.quality.gather

import net.twisterrob.gradle.common.TargetChecker
import org.gradle.api.Task
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.SingleFileReport
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

class QualityTaskReportGatherer<T>(
	private val displayName: String,
	taskType: Class<T>,
	private var parser: Parser
) : TaskReportGatherer<T>(taskType) where
T : Task,
T : TargetChecker,
T : Reporting<out ReportContainer<out SingleFileReport>> {

	override fun getParsableReportLocation(task: T): File =
		@Suppress("detekt.MaxChainedCallsOnSameLine") // Simple enough.
		task.reports.getByName("xml").outputLocation.get().asFile

	override fun getHumanReportLocation(task: T): File =
		@Suppress("detekt.MaxChainedCallsOnSameLine") // Simple enough.
		task.reports.getByName("html").outputLocation.get().asFile

	override fun getName(task: T): String =
		task.checkTargetName

	override fun getDisplayName(task: T): String =
		displayName

	override fun findViolations(report: File): List<Violation> =
		parser.findViolations(NoOpLogger, listOf(report)).toList()
}
