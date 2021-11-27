package net.twisterrob.gradle.quality.tasks

import com.android.build.api.artifact.impl.ArtifactsImpl
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.lint.AndroidLintGlobalTask
import com.android.build.gradle.internal.scope.InternalArtifactType
import net.twisterrob.gradle.common.AndroidVariantApplier
import net.twisterrob.gradle.common.TaskCreationConfiguration
import net.twisterrob.gradle.common.wasLaunchedExplicitly
import net.twisterrob.gradle.quality.QualityPlugin.Companion.REPORT_CONSOLE_TASK_NAME
import net.twisterrob.gradle.quality.QualityPlugin.Companion.REPORT_HTML_TASK_NAME
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName
import se.bjurr.violations.lib.model.Violation
import java.io.File

open class GlobalLintGlobalFinalizerTask : DefaultTask() {

	/**
	 * This should only contain files that will definitely generate (i.e. `if (subTask.enabled)` in [mustRunAfter]).
	 * Currently it is not the case if the submodules are configured from a parent project (see tests).
	 * At the usage we need to double-check if the file existed,
	 * otherwise it'll spam the logs with [java.io.FileNotFoundException]s.
	 */
	@InputFiles
	val xmlReports: MutableList<Provider<RegularFile>> = mutableListOf()

	@TaskAction
	fun failOnFailures() {
		val gatherer = LintReportGatherer()
		val violationsByFile = xmlReports
			.map { it.get().asFile }
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

	internal class Creator : TaskCreationConfiguration<GlobalLintGlobalFinalizerTask> {

		override fun preConfigure(project: Project, taskProvider: TaskProvider<GlobalLintGlobalFinalizerTask>) {
			project.allprojects.forEach { subproject ->
				AndroidVariantApplier(subproject).applyRaw {
					val androidComponents =
						subproject.extensions.getByName<AndroidComponentsExtension<*, *, *>>("androidComponents")

					androidComponents.finalizeDsl {
						// Make sure we have XML output, otherwise can't figure out if it failed.
						// Run this in finalizeDsl rather than just after configuration, to override any normal
						// `android { lintOptions { ... } }` DSL configuration.
						// This is also consistently configuring the task, making it up-to-date when possible.
						it.lint.isAbortOnError = false
						it.lint.xmlReport = true
					}
					androidComponents.onVariants { variant ->
						taskProvider.configure { task ->
							task.xmlReports +=
								(variant.artifacts as ArtifactsImpl)
									.get(InternalArtifactType.LINT_XML_REPORT)
						}
					}
				}
			}
		}

		override fun configure(task: GlobalLintGlobalFinalizerTask) {
			task.group = JavaBasePlugin.VERIFICATION_GROUP
			// A more specific version of mustRunAfter(subproject.tasks.withType(AndroidLintTask::class.java)).
			// That would include lintRelease, lintDebug, lintFixDebug, lintFixRelease.
			task.mustRunAfter(task.xmlReports)
			// Not a necessity, just a convenience, make sure we run after the :*:lint lifecycle tasks.
			// Using .map {} instead of .flatMap {} to prevent configuration of these tasks.
			task.mustRunAfter(task.project.allprojects.map { it.tasks.withType(AndroidLintGlobalTask::class.java) })
		}
	}
}
