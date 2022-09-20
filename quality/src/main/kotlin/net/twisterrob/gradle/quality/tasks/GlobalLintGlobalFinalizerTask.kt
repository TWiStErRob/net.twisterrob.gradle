package net.twisterrob.gradle.quality.tasks

import com.android.build.api.artifact.impl.ArtifactsImpl
import com.android.build.api.variant.TestVariant
import com.android.build.gradle.api.AndroidBasePlugin
import com.android.build.gradle.internal.lint.AndroidLintGlobalTask
import com.android.build.gradle.internal.scope.InternalArtifactType
import net.twisterrob.gradle.android.isAbortOnErrorCompat
import net.twisterrob.gradle.android.androidComponents
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.common.TaskCreationConfiguration
import net.twisterrob.gradle.common.wasLaunchedExplicitly
import net.twisterrob.gradle.internal.android.unwrapCast
import net.twisterrob.gradle.internal.lint.collectXmlReport
import net.twisterrob.gradle.internal.lint.configureXmlReport
import net.twisterrob.gradle.internal.lint.lintGlobalTasks
import net.twisterrob.gradle.quality.QualityPlugin.Companion.REPORT_CONSOLE_TASK_NAME
import net.twisterrob.gradle.quality.QualityPlugin.Companion.REPORT_HTML_TASK_NAME
import net.twisterrob.gradle.quality.gather.LintGlobalReportGathererPre7
import net.twisterrob.gradle.quality.gather.LintReportGatherer
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.withType
import se.bjurr.violations.lib.model.Violation
import java.io.File

open class GlobalLintGlobalFinalizerTask : DefaultTask() {

	/**
	 * This should only contain files that will definitely generate (i.e. `if (subTask.enabled)` in [mustRunAfter]).
	 * Currently, it is not the case if the submodules are configured from a parent project (see tests).
	 * At the usage we need to double-check if the file existed,
	 * otherwise it'll spam the logs with [java.io.FileNotFoundException]s.
	 */
	@InputFiles
	@PathSensitive(PathSensitivity.NONE)
	val xmlReports: MutableList<Provider<RegularFile>> = mutableListOf()

	@TaskAction
	fun failOnFailures() {
		val gatherer =
			if (AGPVersions.CLASSPATH >= AGPVersions.v70x)
				LintReportGatherer()
			else
				LintGlobalReportGathererPre7(ALL_VARIANTS_NAME)
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
			@Suppress("SpreadOperator")
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
				subproject.plugins.withType<AndroidBasePlugin> {
					if (AGPVersions.CLASSPATH >= AGPVersions.v70x) {
						subproject.configureReports(taskProvider)
					} else {
						subproject.configureReportsPre7(taskProvider)
					}
				}
			}
		}

		private fun Project.configureReportsPre7(taskProvider: TaskProvider<GlobalLintGlobalFinalizerTask>) {
			taskProvider.configure { finalizerTask ->
				lintGlobalTasks.configureXmlReport()
				lintGlobalTasks.collectXmlReport {
					finalizerTask.xmlReports.add(it)
				}
			}
		}

		private fun Project.configureReports(taskProvider: TaskProvider<GlobalLintGlobalFinalizerTask>) {
			androidComponents.finalizeDsl {
				// Make sure we have XML output, otherwise can't figure out if it failed.
				// Run this in finalizeDsl rather than just after configuration, to override any normal
				// `android { lintOptions { ... } }` DSL configuration.
				// This is also consistently configuring the task, making it up-to-date when possible.
				it.lint.isAbortOnErrorCompat = false
				it.lint.xmlReport = true
			}
			// AGP 7.4 compatibility: calling onVariants$default somehow changed, being explicit about params helps.
			androidComponents.onVariants(androidComponents.selector().all()) { variant ->
				if (variant is TestVariant) return@onVariants
				taskProvider.configure { task ->
					task.xmlReports += variant.artifacts.unwrapCast<ArtifactsImpl>()
						.get(InternalArtifactType.LINT_XML_REPORT)
				}
			}
		}

		override fun configure(task: GlobalLintGlobalFinalizerTask) {
			task.group = JavaBasePlugin.VERIFICATION_GROUP
			if (AGPVersions.CLASSPATH >= AGPVersions.v70x) {
				// A more specific version of mustRunAfter(subproject.tasks.withType(AndroidLintTask::class.java)).
				// That would include lintRelease, lintDebug, lintFixDebug, lintFixRelease.
				task.mustRunAfter(task.xmlReports)
			}
			// Not a necessity, just a convenience, make sure we run after the :*:lint lifecycle tasks.
			// Using .map {} instead of .flatMap {} to prevent configuration of these tasks.
			task.mustRunAfter(task.project.allprojects.map { it.lintTasks })
		}
	}
}

private val Project.lintTasks: TaskCollection<*>
	get() =
		when {
			AGPVersions.CLASSPATH >= AGPVersions.v70x ->
				this.tasks.withType(AndroidLintGlobalTask::class.java)
			else ->
				this.lintGlobalTasks
		}
