package net.twisterrob.gradle.quality.gather

import io.gitlab.arturbosch.detekt.Detekt
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

class DetektTaskReportGatherer : TaskReportGatherer<Detekt>(Detekt::class.java) {

	override fun getParsableReportLocation(task: Detekt): File =
		task.reports.xml.outputLocation.get().asFile

	override fun getHumanReportLocation(task: Detekt): File =
		task.reports.html.outputLocation.get().asFile

	override fun getName(task: Detekt): String =
		"task.checkTargetName"

	override fun getDisplayName(task: Detekt): String =
		"Detekt $task"

	override fun findViolations(report: File): List<Violation> =
		Parser.CHECKSTYLE.findViolations(NoOpLogger, listOf(report)).toList()
}
