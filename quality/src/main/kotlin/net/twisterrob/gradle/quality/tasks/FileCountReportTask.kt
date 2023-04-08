package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.dsl.reporting
import net.twisterrob.gradle.quality.Violations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import java.io.File

@CacheableTask
abstract class FileCountReportTask : BaseViolationsTask() {

	private val outputFile: File
		get() = output.asFile.get()

	@get:OutputFile
	abstract val output: RegularFileProperty

	init {
		output.convention(project.reporting.baseDirectory.file("violations.count"))
	}

	override fun processViolations(violations: Grouper.Start<Violations>) {
		val count = violations.list.sumOf { it.violations.orEmpty().size }
		outputFile.writeText(count.toString())
	}
}
