package net.twisterrob.gradle.quality

import net.twisterrob.gradle.checkstyle.CheckStylePlugin
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.common.BaseExposedPlugin
import net.twisterrob.gradle.common.registerTask
import net.twisterrob.gradle.pmd.PmdPlugin
import net.twisterrob.gradle.quality.tasks.GlobalTestFinalizerTask
import net.twisterrob.gradle.quality.tasks.HtmlReportTask
import net.twisterrob.gradle.quality.tasks.ValidateViolationsTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class QualityPlugin : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		//project.apply<GradlePlugin>()
		project.extensions.create("quality", QualityExtension::class.java, project)
		// needed for accessing ReportingExtension to get `build/reporting` folder
		project.plugins.apply("org.gradle.reporting-base")
		project.apply<CheckStylePlugin>()
		project.apply<PmdPlugin>()

		if (project.rootProject == project) {
			project.tasks.register(REPORT_CONSOLE_TASK_NAME, ValidateViolationsTask::class.java)
			project.tasks.register(REPORT_HTML_TASK_NAME, HtmlReportTask::class.java)
			if (AGPVersions.isAvailable) {
				project.plugins.apply(LintPlugin::class.java)
			}
			project.registerTask("testReport", GlobalTestFinalizerTask.Creator())
		}
	}

	companion object {
		const val REPORT_CONSOLE_TASK_NAME: String = "violationReportConsole"
		const val REPORT_HTML_TASK_NAME: String = "violationReportHtml"
	}
}
