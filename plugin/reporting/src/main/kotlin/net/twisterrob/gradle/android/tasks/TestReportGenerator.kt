package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.internal.test.report.ReportType
import com.android.build.gradle.internal.test.report.ResilientTestReport
import com.android.utils.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class TestReportGenerator : DefaultTask() {

	@get:InputDirectory
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val input: DirectoryProperty

	@get:OutputDirectory
	abstract val output: DirectoryProperty

	@get:Input
	abstract val type: Property<ReportType>

	init {
		type.convention(ReportType.SINGLE_FLAVOR)
	}

	@TaskAction
	fun generate() {
		val input = this.input.get().asFile
		val output = this.output.get().asFile
		val type = this.type.get()

		FileUtils.cleanOutputDir(output)
		val report = ResilientTestReport(type, input, output)

		report.generateReport()
	}
}
