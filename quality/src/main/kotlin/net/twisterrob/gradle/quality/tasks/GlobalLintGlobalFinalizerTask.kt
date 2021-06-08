package net.twisterrob.gradle.quality.tasks

import com.android.build.gradle.tasks.LintGlobalTask
import net.twisterrob.gradle.common.AndroidVariantApplier
import net.twisterrob.gradle.common.wasLaunchedExplicitly
import net.twisterrob.gradle.common.xmlOutput
import net.twisterrob.gradle.quality.QualityPlugin
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
			val hasConsole = project.gradle.taskGraph.hasTask(":" + QualityPlugin.REPORT_CONSOLE_TASK_NAME)
			val hasHtml = project.gradle.taskGraph.hasTask(":" + QualityPlugin.REPORT_HTML_TASK_NAME)
			val postfix = if (!hasConsole && !hasHtml) {
				"${System.lineSeparator()}To get a full breakdown and listing, " +
						"execute ${QualityPlugin.REPORT_CONSOLE_TASK_NAME} or ${QualityPlugin.REPORT_HTML_TASK_NAME}."
			} else {
				"" // No message, it's already going to execute.
			}
			val message = "Ran lint on subprojects: ${totalCount} issues found${System.lineSeparator()}" +
					violationsByFile.entries.joinToString(
						prefix = "See reports in subprojects:${System.lineSeparator()}",
						separator = System.lineSeparator(),
						postfix = postfix,
					) { (report, violations) ->
						"${report} (${violations.size})"
					}
			if (this.wasLaunchedExplicitly) {
				throw GradleException(message)
			} else {
				logger.warn(message)
			}
		}
	}
}
