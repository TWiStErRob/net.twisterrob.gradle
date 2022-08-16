package net.twisterrob.gradle.quality.gather

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import se.bjurr.violations.lib.model.Violation
import java.io.File

abstract class TaskReportGatherer<T>(
	private val taskType: Class<T>
) where T : Task {

	abstract fun getParsableReportLocation(task: T): File

	abstract fun getHumanReportLocation(task: T): File

	abstract fun getName(task: T): String

	abstract fun getDisplayName(task: T): String

	fun getViolations(task: T): List<Violation>? {
		val report = getParsableReportLocation(task)
		return if (report.exists()) {
			findViolations(report)
		} else {
			//println("${this} > report '${report}' does not exist.")
			null
		}
	}

	abstract fun findViolations(report: File): List<Violation>

	open fun allTasksFrom(project: Project): TaskCollection<T> =
		project.tasks.withType(taskType)
}
