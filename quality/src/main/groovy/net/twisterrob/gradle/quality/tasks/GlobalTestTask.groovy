package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.quality.gather.TestReportGatherer
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import se.bjurr.violations.lib.model.SEVERITY
import se.bjurr.violations.lib.model.Violation

class GlobalTestTask extends TestReport {

	GlobalTestTask() {
		destinationDir = project.file("${project.buildDir}/reports/tests/allTests")
		project.afterEvaluate {
			reportOn project.allprojects.collectMany {Project subproject ->
				subproject.tasks.withType(Test) {Test testTask ->
					// let the build finish running all tests
					testTask.ignoreFailures = true
					// make sure we have xml output, otherwise can't figure out if test failed
					testTask.reports.junitXml.enabled = true
				}
			}
		}
		doLast { // fail the build if there were failures
			def gatherer = new TestReportGatherer()
			def violations = testResultDirs.files.collectMany {
				// reportOn above added the binary folder, so the XMLs are one up
				(Collection<Violation>)gatherer.findViolations(new File(it, '..'))
			}
			def errors = (violations.groupBy {Violation violation -> violation.severity})[SEVERITY.ERROR]
			if (errors.size() > 0) {
				def report = new File(destinationDir, 'index.html').toURI()
				throw new GradleException("There were ${errors.size()} failing tests. See the report at: ${report}")
			}
		}
	}
}
