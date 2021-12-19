package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.compat.conventionCompat
import net.twisterrob.gradle.compat.newOutputFileCompat
import net.twisterrob.gradle.dsl.reporting
import net.twisterrob.gradle.quality.Violations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import java.io.File

open class FileCountReportTask : ValidateViolationsTask() {

	private val outputFile: File
		get() = output.asFile.get()

	@get:OutputFile
	val output: RegularFileProperty = newOutputFileCompat()

	init {
		output.conventionCompat(project.reporting.baseDirectory.file("violations.count"))
	}

	override fun processViolations(violations: Grouper.Start<Violations>) {
		val count = violations.list.sumOf { it.violations.orEmpty().size }
		outputFile.writeText(count.toString())
	}
}
