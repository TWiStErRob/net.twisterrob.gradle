package net.twisterrob.gradle.quality.gather

import com.android.build.gradle.tasks.LintGlobalTask
import net.twisterrob.gradle.internal.lint.htmlOutput
import net.twisterrob.gradle.internal.lint.xmlOutput
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

class LintGlobalReportGathererPre7(
	private val name: String
) : TaskReportGatherer<LintGlobalTask>(LintGlobalTask::class.java) {

	override fun getParsableReportLocation(task: LintGlobalTask): File =
		task.xmlOutput

	override fun getHumanReportLocation(task: LintGlobalTask): File =
		task.htmlOutput

	override fun getName(task: LintGlobalTask): String =
		name

	override fun getDisplayName(task: LintGlobalTask): String =
		"lint"

	override fun findViolations(report: File): List<Violation> =
		Parser.ANDROIDLINT.findViolations(NoOpLogger, listOf(report)).toList()
}
