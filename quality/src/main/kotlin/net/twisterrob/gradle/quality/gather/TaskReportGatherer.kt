package net.twisterrob.gradle.quality.gather

import org.gradle.api.Task
import se.bjurr.violations.lib.model.Violation
import java.io.File

abstract class TaskReportGatherer<T>(
		var displayName: String,
		var taskType: Class<T>
) where T : Task {

	abstract fun getParsableReportLocation(task: T): File

	abstract fun getHumanReportLocation(task: T): File

	abstract fun getName(task: T): String

	fun getViolations(task: T): List<Violation>? {
		val report = getParsableReportLocation(task)
		return if (report.exists()) {
			findViolations(report)
		} else {
			//logger.warn "${parser} report: '${report}' does not exist"
			null
		}
	}

	abstract fun findViolations(report: File): List<Violation>
}
