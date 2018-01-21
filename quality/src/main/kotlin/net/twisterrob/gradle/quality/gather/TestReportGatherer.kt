package net.twisterrob.gradle.quality.gather

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

	override fun getReportLocation(task: T) = task.reports.junitXml.destination as File

	override fun getName(task: T) : String = task.path

	override fun findViolations(report: File): List<Violation> {
		val reportFiles = report.listFiles({ file -> file.isFile })
		return reportFiles.flatMap {
			parser.parseReportOutput(it.readText())
		}
	}
}
