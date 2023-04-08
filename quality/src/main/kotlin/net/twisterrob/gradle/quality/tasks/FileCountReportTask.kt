package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.dsl.reporting
import net.twisterrob.gradle.internal.safeWriteText
import net.twisterrob.gradle.quality.Violations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile

@CacheableTask
abstract class FileCountReportTask : BaseViolationsTask() {

	@get:OutputFile
	abstract val output: RegularFileProperty

	init {
		output.convention(project.reporting.baseDirectory.file("violations.count"))
	}

	override fun processViolations(violations: Grouper.Start<Violations>) {
		val count = violations.list.sumOf { it.violations.orEmpty().size }
		output.safeWriteText(count.toString())
	}
}
