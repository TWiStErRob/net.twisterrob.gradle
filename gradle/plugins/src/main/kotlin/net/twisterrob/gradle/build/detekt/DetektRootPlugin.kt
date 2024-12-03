package net.twisterrob.gradle.build.detekt

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektReport
import io.gitlab.arturbosch.detekt.extensions.DetektReports
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.initialization.IncludedBuild
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
internal abstract class DetektRootPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		require(project.subprojects.isNotEmpty()) {
			"${this} can only be applied to the root or container project."
		}
		configureDetektReportMerging(project)
	}

	@Suppress("detekt.NamedArguments")
	private fun configureDetektReportMerging(project: Project) {
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
	project.tasks.register<ReportMergeTask>("detektReportMerge${mergedExtension.replaceFirstChar(Char::uppercase)}") {
		val detektReportMergeTask = this@register
		output = project.layout.buildDirectory.file("reports/detekt/merge.${mergedExtension}")
		// Intentionally eager: at the point detektReportMergeXml is configured,
		// we need to know about all the Detekt tasks for their report locations.
		project.evaluationDependsOnChildren()
		project.allprojects
			.flatMap { it.tasks.withType<Detekt>() } // Forces to create the tasks.
			.onEach { it.reports { report().required = true } }
			.forEach { detektReportingTask ->
				detektReportMergeTask.mustRunAfter(detektReportingTask)
				detektReportMergeTask.input.from(detektReportingTask.reportFile())
			}
		val mergeIncludedBuilds = project.providers
			.gradleProperty("net.twisterrob.gradle.build.detektReportMergeIncludedBuilds")
			.map(String::toBooleanStrict)
		if (mergeIncludedBuilds.get()) {
			mergeReportFromIncludedBuilds(detektReportMergeTask, project.gradle, mergedExtension)
		}
	}
}

private fun mergeReportFromIncludedBuilds(
	detektReportMergeTask: ReportMergeTask,
	gradle: Gradle,
	mergedExtension: String,
) {
	@Suppress("detekt.NamedArguments")
	gradle.includedBuilds.forEach { includedBuild ->
		detektReportMergeTask.mergeReportFrom(includedBuild, ":detekt", "detekt.${mergedExtension}")
		detektReportMergeTask.mergeReportFrom(includedBuild, ":detektMain", "main.${mergedExtension}")
		detektReportMergeTask.mergeReportFrom(includedBuild, ":detektTest", "test.${mergedExtension}")
	}
}

private fun ReportMergeTask.mergeReportFrom(
	includedBuild: IncludedBuild,
	detektTaskName: String,
	detektReportName: String,
) {
	val detektTask = includedBuild.task(detektTaskName)
	this.dependsOn(detektTask) // mustRunAfter not possible since Gradle 8.0!
	val reportPath = "build/reports/detekt/${detektReportName}"
	this.input.from(includedBuild.projectDir.resolve(reportPath))
}

