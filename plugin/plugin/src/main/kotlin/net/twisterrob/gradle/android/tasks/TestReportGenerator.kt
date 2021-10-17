package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.internal.test.report.ReportType
import com.android.build.gradle.internal.test.report.ResilientTestReport
import com.android.utils.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class TestReportGenerator : DefaultTask() {

	@InputDirectory
	lateinit var input: File

	@OutputDirectory
	lateinit var output: File

	@Input
	var type = ReportType.SINGLE_FLAVOR

	@TaskAction
	fun generate() {
		FileUtils.cleanOutputDir(output)
		val report = ResilientTestReport(type, input, output)
		report.generateReport()
	}
}
