package net.twisterrob.gradle.quality.violations

import net.twisterrob.gradle.quality.tasks.BaseViolationsTask.Result.Project
import java.io.File

internal class ModuleDeducer(modules: List<Project>) {
	private val lookup = modules
		.associateBy { it.projectDir }

	fun deduce(originatingModule: Project, file: File): Project =
		generateSequence(file) { it.parentFile }
			.mapNotNull { lookup[it] }
			.firstOrNull()
			?: originatingModule
}
