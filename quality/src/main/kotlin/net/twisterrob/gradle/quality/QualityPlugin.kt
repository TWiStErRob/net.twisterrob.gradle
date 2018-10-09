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
			project.tasks.register("violationReportConsole", ValidateViolationsTask::class.java)
			project.tasks.register("violationReportHtml", HtmlReportTask::class.java)
		}
		project.afterEvaluate {
			if (project.tasks.findByName("lint") == null) {
				project.tasks.register("lint", GlobalLintGlobalFinalizerTask::class.java)
			}
		}
	}
}
