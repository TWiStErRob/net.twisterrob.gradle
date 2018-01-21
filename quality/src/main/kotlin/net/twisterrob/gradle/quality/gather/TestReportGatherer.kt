package net.twisterrob.gradle.quality.gather

import net.twisterrob.gradle.quality.parsers.JUnitParser
import org.gradle.api.tasks.testing.Test
import se.bjurr.violations.lib.parsers.ViolationsParser
import java.io.File

class TestReportGatherer<T>(
		displayName: String,
		taskType: Class<T>
) : TaskReportGatherer<T>(displayName, taskType) where T : Test {

	private val parser: ViolationsParser = JUnitParser()

	override fun getReportLocation(task: T): File =
			task.reports.junitXml.destination

	override fun getName(task: T): String =
			task.path

	override fun findViolations(report: File) =
			report
					.listFiles({ file -> file.isFile })
					.flatMap { file -> parser.parseReportOutput(file.readText()) }
}
