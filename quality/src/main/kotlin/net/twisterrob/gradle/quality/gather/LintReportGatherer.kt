package net.twisterrob.gradle.quality.gather

import com.android.build.gradle.internal.lint.AndroidLintTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskCollection
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

internal class LintReportGatherer(
) : TaskReportGatherer<AndroidLintTask>(AndroidLintTask::class.java) {

	override fun getParsableReportLocation(task: AndroidLintTask): File =
		task.xmlReportOutputFile.asFile.get()

	override fun getHumanReportLocation(task: AndroidLintTask): File =
		task.htmlReportOutputFile.asFile.get()

	override fun getName(task: AndroidLintTask): String =
		task.variantName

	override fun getDisplayName(task: AndroidLintTask): String =
		"lintVariant"

	override fun findViolations(report: File): List<Violation> =
		Parser.ANDROIDLINT.findViolations(NoOpLogger, listOf(report)).toList()

	override fun allTasksFrom(project: Project): TaskCollection<AndroidLintTask> =
		super.allTasksFrom(project)
			.matching { it.xmlReportOutputFile.isPresent }
}
