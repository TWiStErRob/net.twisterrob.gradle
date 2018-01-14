package net.twisterrob.gradle.quality.gather

import org.gradle.api.Task
import se.bjurr.violations.lib.model.Violation

abstract class TaskReportGatherer<T extends Task> {

	Class<T> taskType
	String displayName

	abstract File getReportLocation(T task)

	abstract String getName(T task)

	List<Violation> getViolations(T task) {
		File report = getReportLocation(task)
		List<Violation> violations
		if (report.exists()) {
			violations = findViolations(report)
		} else {
			//logger.warn "${parser} report: '${report}' does not exist"
			violations = null
		}
		return violations
	}

	protected abstract List<Violation> findViolations(File report)
}
