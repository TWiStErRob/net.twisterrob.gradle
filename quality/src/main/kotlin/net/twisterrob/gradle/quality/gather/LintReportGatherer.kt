package net.twisterrob.gradle.quality.gather

import com.android.build.gradle.tasks.LintBaseTask
import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.common.getXmlOutput
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser
import java.io.File

class LintReportGatherer<T>(
		displayName: String,
		taskType: Class<T>
) : TaskReportGatherer<T>(displayName, taskType) where T : LintBaseTask {

	override fun getReportLocation(task: T): File = when (task) {
		is LintGlobalTask -> getXmlOutput(task)
		is LintPerVariantTask -> getXmlOutput(task)
		else -> throw IllegalArgumentException("${task.path} (${task::class}) is not recognized as a lint task")
	}

	override fun getName(task: T): String = when (task) {
		is LintGlobalTask -> ALL_VARIANTS_NAME
		is LintPerVariantTask -> task.variantName
		else -> throw IllegalArgumentException("${task.path} (${task::class}) is not recognized as a lint task")
	}

	override fun findViolations(report: File): List<Violation> =
			Parser.ANDROIDLINT.findViolations(listOf(report))
}
