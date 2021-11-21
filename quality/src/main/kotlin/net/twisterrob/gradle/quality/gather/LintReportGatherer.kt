package net.twisterrob.gradle.quality.gather

import com.android.build.gradle.tasks.LintBaseTask
import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.internal.lint.androidVariantName
import net.twisterrob.gradle.internal.lint.htmlOutput
import net.twisterrob.gradle.internal.lint.xmlOutput
import org.gradle.api.Task
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

class LintReportGatherer<T>(
	displayName: String,
	taskType: Class<T>
) : TaskReportGatherer<T>(displayName, taskType) where T : LintBaseTask {

	override fun getParsableReportLocation(task: T): File =
		when (task) {
			is LintGlobalTask -> task.xmlOutput
			is LintPerVariantTask -> task.xmlOutput
			else -> unsupported(task)
		}

	override fun getHumanReportLocation(task: T): File =
		when (task) {
			is LintGlobalTask -> task.htmlOutput
			is LintPerVariantTask -> task.htmlOutput
			else -> unsupported(task)
		}

	override fun getName(task: T): String =
		when (task) {
			is LintGlobalTask -> ALL_VARIANTS_NAME
			is LintPerVariantTask -> task.androidVariantName!!
			else -> unsupported(task)
		}

	override fun findViolations(report: File): List<Violation> =
		Parser.ANDROIDLINT.findViolations(listOf(report))
}

private fun unsupported(task: Task): Nothing {
	throw IllegalArgumentException("${task.path} (${task::class}) is not recognized as a lint task")
}
