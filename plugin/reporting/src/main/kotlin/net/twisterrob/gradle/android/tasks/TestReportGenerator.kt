package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.internal.test.report.ReportType
import com.android.build.gradle.internal.test.report.ResilientTestReport
import com.android.utils.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class TestReportGenerator : DefaultTask() {

	@get:InputDirectory
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val input: DirectoryProperty

	@get:OutputDirectory
	abstract val output: DirectoryProperty

	@Input
	var type: ReportType = ReportType.SINGLE_FLAVOR

	@TaskAction
	fun generate() {
		val inp = input.get().asFile
		val out = output.get().asFile

		FileUtils.cleanOutputDir(out)
		val report = ResilientTestReport(type, inp, out)

		report.generateReport()
	}
}
