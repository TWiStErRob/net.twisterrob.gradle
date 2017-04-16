package net.twisterrob.gradle.android.tasks

import com.android.build.gradle.internal.test.report.*
import com.android.utils.FileUtils
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

@CompileStatic
public class TestReportGenerator extends DefaultTask {
	@InputDirectory File input
	@OutputDirectory File output
	@Input ReportType type = ReportType.SINGLE_FLAVOR

	@TaskAction
	public void generate() {
		FileUtils.cleanOutputDir(output);
		ResilientTestReport report = new ResilientTestReport(type, input, output);
		report.generateReport();
	}
}
