package net.twisterrob.gradle.quality.tasks

import com.android.build.gradle.tasks.LintGlobalTask
import net.twisterrob.gradle.common.AndroidVariantApplier
import net.twisterrob.gradle.common.wasLaunchedExplicitly
import net.twisterrob.gradle.common.xmlOutput
import net.twisterrob.gradle.quality.QualityPlugin.Companion.REPORT_CONSOLE_TASK_NAME
import net.twisterrob.gradle.quality.QualityPlugin.Companion.REPORT_HTML_TASK_NAME
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskAction
import se.bjurr.violations.lib.model.Violation
import java.io.File

open class GlobalLintGlobalFinalizerTask : DefaultTask() {

	/**
	 * This should only contain files that will definitely generate (i.e. `if (subTask.enabled)` in [mustRunAfter]).
	 * Currently it is not the case if the submodules are configured from a parent project (see tests).
	 * At the usage we need to double-check if the file existed,
	 * otherwise it'll spam the logs with [java.io.FileNotFoundException]s.
	 */
	private val xmlReports = mutableListOf<File>()

	init {
		group = JavaBasePlugin.VERIFICATION_GROUP
		project.allprojects.forEach { subproject ->
			AndroidVariantApplier(subproject).applyAfterPluginConfigured(Action {
				mustRunAfter(subproject.tasks.withType(LintGlobalTask::class.java) { subTask ->
					subTask.lintOptions.isAbortOnError = subTask.wasLaunchedExplicitly
					// make sure we have xml output, otherwise can't figure out if it failed
					subTask.lintOptions.xmlReport = true
					xmlReports += subTask.xmlOutput
				})
			})
		}
	}

	@TaskAction
	fun failOnFailures() {
		val gatherer = LintReportGatherer("lint", LintGlobalTask::class.java)
		val violationsByFile = xmlReports
			.filter(File::exists)
			.associateBy({ it }) { gatherer.findViolations(it) }
		val totalCount = violationsByFile.values.sumBy { violations: List<Violation> -> violations.size }
		if (totalCount > 0) {
			val hasConsole = project.gradle.taskGraph.hasTask(":$REPORT_CONSOLE_TASK_NAME")
			val hasHtml = project.gradle.taskGraph.hasTask(":$REPORT_HTML_TASK_NAME")
			val projectReports = violationsByFile.entries
				.map { (report, violations) ->
					"${report} (${violations.size})"
				}
			val lines = listOfNotNull(
				"Ran lint on subprojects: ${totalCount} issues found.",
				"See reports in subprojects:",
				*projectReports.toTypedArray(),
				if (hasConsole || hasHtml) {
					null // No message, it's already going to execute.
				} else {
					"To get a full breakdown and listing, execute $REPORT_CONSOLE_TASK_NAME or $REPORT_HTML_TASK_NAME."
				}
			)
			val message = lines.joinToString(separator = System.lineSeparator())
			if (this.wasLaunchedExplicitly) {
				throw GradleException(message)
			} else {
				logger.warn(message)
			}
		}
	}
}
