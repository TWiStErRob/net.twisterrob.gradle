package net.twisterrob.gradle.quality.gather

import groovy.transform.CompileDynamic
import net.twisterrob.gradle.common.TargetChecker
import org.gradle.api.Task
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.SingleFileReport
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.reports.Parser

class QualityTaskReportGatherer<T extends Task & TargetChecker & Reporting<? extends ReportContainer<? extends SingleFileReport>>>
		extends TaskReportGatherer<T> {

	Parser parser

	@Override
	File getReportLocation(T task) {
		def reportTask = (Reporting<? extends ReportContainer<? extends SingleFileReport>>)task
		return reportTask.reports.getByName('xml').destination
	}

	@CompileDynamic
	@Override
	String getName(T task) {
		return task.checkTargetName?: 'TODO'
	}

	List<Violation> findViolations(File report) {
		parser.findViolations(Collections.<File> singletonList(report))
	}
}
