package net.twisterrob.gradle.quality.tasks

import com.android.build.gradle.BasePlugin
import com.android.build.gradle.tasks.LintGlobalTask
import net.twisterrob.gradle.common.AndroidVariantApplier
import net.twisterrob.gradle.common.wasExplicitlyLaunched
import net.twisterrob.gradle.common.xmlOutput
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.Violation
import java.io.File

open class GlobalLintGlobalFinalizerTask : DefaultTask() {

	private val xmlReports = mutableListOf<File>()

	init {
		project.allprojects.forEach { subproject ->
			AndroidVariantApplier(subproject).applyAfterPluginConfigured(Action { _: BasePlugin ->
				mustRunAfter(subproject.tasks.withType(LintGlobalTask::class.java) { subTask ->
					subTask.lintOptions.isAbortOnError = subTask.wasExplicitlyLaunched
					// make sure we have xml output, otherwise can't figure out if it failed
					subTask.lintOptions.xmlReport = true
					xmlReports += subTask.xmlOutput
				})
			})
		}
	}

	@Suppress("unused")
	@TaskAction
	fun failOnFailures() {
		val gatherer = LintReportGatherer("lint", LintGlobalTask::class.java)
		val violationsByFile = xmlReports.associateBy({ it }) { gatherer.findViolations(it) }
		val totalCount = violationsByFile.values.sumBy { violations: List<Violation> -> violations.size }
		if (totalCount > 0) {
			val reportsWithCounts = violationsByFile.map { (report, violations) -> "${report} (${violations.size})" }
			throw  GradleException(
					"Ran lint on subprojects: ${totalCount} issues found${System.lineSeparator()}" +
							"See reports in subprojects:${System.lineSeparator()}" +
							reportsWithCounts.joinToString(System.lineSeparator()))
		}
	}
}
