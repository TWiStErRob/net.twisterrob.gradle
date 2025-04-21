package net.twisterrob.gradle.quality.gather

import net.twisterrob.gradle.common.listFilesInDirectory
import org.gradle.api.tasks.testing.Test
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

class TestReportGatherer<T>(
	taskType: Class<T>
) : TaskReportGatherer<T>(taskType) where T : Test {

	override fun getParsableReportLocation(task: T): File =
		@Suppress("detekt.MaxChainedCallsOnSameLine") // Simple enough.
		task.reports.junitXml.outputLocation.get().asFile

	override fun getHumanReportLocation(task: T): File =
		@Suppress("detekt.MaxChainedCallsOnSameLine") // Simple enough.
		task.reports.html.outputLocation.get().asFile

	override fun getName(task: T): String =
		task.path

	override fun getDisplayName(task: T): String =
		"test"

	override fun findViolations(report: File): List<Violation> {
		val reports = report.listFilesInDirectory(File::isFile).toList()
		return Parser.JUNIT.findViolations(NoOpLogger, reports).toList()
	}
}
