package net.twisterrob.gradle.quality.gather

import net.twisterrob.gradle.common.listFilesInDirectory
import net.twisterrob.gradle.compat.getOutputLocationCompat
import net.twisterrob.gradle.quality.parsers.JUnitParser
import org.gradle.api.tasks.testing.Test
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.parsers.ViolationsParser
import java.io.File

class TestReportGatherer<T>(
	displayName: String,
	taskType: Class<T>
) : TaskReportGatherer<T>(displayName, taskType) where T : Test {

	private val parser: ViolationsParser = JUnitParser()

	override fun getParsableReportLocation(task: T): File =
		task.reports.junitXml.getOutputLocationCompat()

	override fun getHumanReportLocation(task: T): File =
		task.reports.html.getOutputLocationCompat()

	override fun getName(task: T): String =
		task.path

	override fun findViolations(report: File): List<Violation> =
		report
			.listFilesInDirectory(File::isFile)
			.flatMap { file -> parser.parseReportOutput(file.readText()) }
}
