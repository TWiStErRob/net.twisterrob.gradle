package net.twisterrob.gradle.quality.gather

import net.twisterrob.gradle.quality.parsers.JUnitParser
import org.gradle.api.tasks.testing.Test
import se.bjurr.violations.lib.model.Violation
import se.bjurr.violations.lib.parsers.ViolationsParser

class TestReportGatherer<T extends Test> extends TaskReportGatherer<T> {

	private final ViolationsParser parser = new JUnitParser()

	@Override
	File getReportLocation(T task) {
		return task.reports.junitXml.destination
	}

	@Override
	String getName(T task) {
		return task.path
	}

	@Override
	List<Violation> findViolations(File reportDir) {
		def reportFiles = reportDir.listFiles({File file -> file.isFile()} as FileFilter)
		return reportFiles.collectMany {
			parser.parseReportOutput(it.text)
		}
	}
}
