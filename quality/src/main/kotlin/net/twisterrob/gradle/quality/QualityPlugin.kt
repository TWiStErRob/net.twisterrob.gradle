package net.twisterrob.gradle.quality

import net.twisterrob.gradle.checkstyle.CheckStylePlugin
import net.twisterrob.gradle.common.BaseExposedPlugin
import net.twisterrob.gradle.pmd.PmdPlugin
import net.twisterrob.gradle.quality.tasks.GlobalLintGlobalFinalizerTask
import net.twisterrob.gradle.quality.tasks.HtmlReportTask
import net.twisterrob.gradle.quality.tasks.ValidateViolationsTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class QualityPlugin : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.extensions.create("quality", QualityExtension::class.java, project)
		// needed for accessing ReportingExtension to get `build/reporting` folder
		project.plugins.apply("org.gradle.reporting-base")
		project.apply<CheckStylePlugin>()
		project.apply<PmdPlugin>()

		if (project.rootProject == project) {
			project.tasks.register(REPORT_CONSOLE_TASK_NAME, ValidateViolationsTask::class.java)
			project.tasks.register(REPORT_HTML_TASK_NAME, HtmlReportTask::class.java)
			project.afterEvaluate {
				// TODO only when lint is on classpath? what about normal Java projects?
				// TODO move to LintPlugin?
				if (project.tasks.findByName("lint") == null) {
					project.tasks.register("lint", GlobalLintGlobalFinalizerTask::class.java)
				}
			}
		}
	}

	companion object {
		const val REPORT_CONSOLE_TASK_NAME = "violationReportConsole"
		const val REPORT_HTML_TASK_NAME = "violationReportHtml"
	}
}
