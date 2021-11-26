package net.twisterrob.gradle.quality.gather

import com.android.build.gradle.internal.lint.AndroidLintTask
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

internal class LintReportGatherer(
) : TaskReportGatherer<AndroidLintTask>("lintVariant", AndroidLintTask::class.java) {

	override fun getParsableReportLocation(task: AndroidLintTask): File =
		task.xmlReportOutputFile.asFile.get()

	override fun getHumanReportLocation(task: AndroidLintTask): File =
		task.htmlReportOutputFile.asFile.get()

	override fun getName(task: AndroidLintTask): String =
		task.variantName

	override fun findViolations(report: File): List<Violation> =
		Parser.ANDROIDLINT.findViolations(listOf(report))
}
