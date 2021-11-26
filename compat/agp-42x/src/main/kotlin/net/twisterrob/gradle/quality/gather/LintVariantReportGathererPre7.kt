package net.twisterrob.gradle.quality.gather

import com.android.build.gradle.tasks.LintPerVariantTask
import net.twisterrob.gradle.internal.lint.androidVariantName
import net.twisterrob.gradle.internal.lint.htmlOutput
import net.twisterrob.gradle.internal.lint.xmlOutput
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

class LintVariantReportGathererPre7(
) : TaskReportGatherer<LintPerVariantTask>("lintVariant", LintPerVariantTask::class.java) {

	override fun getParsableReportLocation(task: LintPerVariantTask): File =
		task.xmlOutput

	override fun getHumanReportLocation(task: LintPerVariantTask): File =
		task.htmlOutput

	override fun getName(task: LintPerVariantTask): String =
		task.androidVariantName!!

	override fun findViolations(report: File): List<Violation> =
		Parser.ANDROIDLINT.findViolations(listOf(report))
}
