package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.internal.test.report.ReportType
import com.android.build.gradle.internal.test.report.ResilientTestReport
import com.android.utils.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class TestReportGenerator : DefaultTask() {

	@get:InputDirectory
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract var input: File

	@get:OutputDirectory
	abstract var output: File

	@Input
	var type: ReportType = ReportType.SINGLE_FLAVOR

	@TaskAction
	fun generate() {
		FileUtils.cleanOutputDir(output)
		val report = ResilientTestReport(type, input, output)
		report.generateReport()
	}
}
