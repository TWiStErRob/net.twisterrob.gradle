package net.twisterrob.gradle.quality.gather

import net.twisterrob.gradle.common.TargetChecker
import org.gradle.api.Task
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.SingleFileReport
import se.bjurr.violations.lib.reports.Parser
import java.io.File

class QualityTaskReportGatherer<T>(
		displayName: String,
		taskType: Class<T>,
		var parser: Parser
) : TaskReportGatherer<T>(displayName, taskType) where
T : Task,
T : TargetChecker,
T : Reporting<out ReportContainer<SingleFileReport>> {


	override fun getReportLocation(task: T): File {
		return task.reports.getByName("xml").destination
	}

	override fun getName(task: T) = task.checkTargetName

	override fun findViolations(report: File) = parser.findViolations(listOf(report))
}
