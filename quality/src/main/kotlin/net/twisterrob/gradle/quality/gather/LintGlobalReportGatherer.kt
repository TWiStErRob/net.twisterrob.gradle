package net.twisterrob.gradle.quality.gather

import com.android.build.gradle.tasks.LintGlobalTask
import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.internal.lint.htmlOutput
import net.twisterrob.gradle.internal.lint.xmlOutput
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

internal class LintGlobalReportGatherer(
) : TaskReportGatherer<LintGlobalTask>("lint", LintGlobalTask::class.java) {

	override fun getParsableReportLocation(task: LintGlobalTask): File =
		task.xmlOutput

	override fun getHumanReportLocation(task: LintGlobalTask): File =
		task.htmlOutput

	override fun getName(task: LintGlobalTask): String =
		ALL_VARIANTS_NAME

	override fun findViolations(report: File): List<Violation> =
		Parser.ANDROIDLINT.findViolations(listOf(report))
}
