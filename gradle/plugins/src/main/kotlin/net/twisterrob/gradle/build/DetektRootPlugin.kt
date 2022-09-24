package net.twisterrob.gradle.build

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektReport
import io.gitlab.arturbosch.detekt.extensions.DetektReports
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

internal class DetektRootPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		require(project.subprojects.isNotEmpty()) {
			"${this} can only be applied to the root or container project."
		}
		configureDetektReportMerging(project, "sarif", DetektReports::sarif, Detekt::sarifReportFile)
		configureDetektReportMerging(project, "xml", DetektReports::xml, Detekt::xmlReportFile)
	}
}

private fun configureDetektReportMerging(
	project: Project,
	mergedExtension: String,
	report: DetektReports.() -> DetektReport,
	reportFile: Detekt.() -> Provider<RegularFile>
) {
	project.tasks.register<ReportMergeTask>("detektReportMerge${mergedExtension.capitalized()}") {
		val detektReportMergeTask = this@register
		output.set(project.buildDir.resolve("reports/detekt/merge.${mergedExtension}"))
		// Intentionally eager: at the point detektReportMergeXml is configured,
		// we need to know about all the Detekt tasks for their report locations.
		project.evaluationDependsOnChildren()
		project.allprojects
			.flatMap { it.tasks.withType<Detekt>() } // Forces to create the tasks.
			.onEach { it.reports { report().required.set(true) } }
			.forEach { detektReportingTask ->
				detektReportMergeTask.mustRunAfter(detektReportingTask)
				detektReportMergeTask.input.from(detektReportingTask.reportFile())
			}
	}
}
