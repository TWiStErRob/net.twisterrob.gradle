package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.wasLaunchedOnly
import net.twisterrob.gradle.compat.setRequired
import net.twisterrob.gradle.quality.gather.TestReportGatherer
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import se.bjurr.violations.lib.model.SEVERITY
import java.io.File

open class GlobalTestFinalizerTask : TestReport() {

	init {
		destinationDir = project.file("${project.buildDir}/reports/tests/allTests")
		project.afterEvaluate {
			reportOn(project.allprojects.flatMap { subproject ->
				subproject.tasks.withType(Test::class.java) { subTask ->
					// let the build finish running all tests
					subTask.ignoreFailures = !subTask.wasLaunchedOnly
					// make sure we have xml output, otherwise can't figure out if test failed
					subTask.reports.junitXml.setRequired(true)
				}
			})
		}
	}

	@Suppress("unused")
	@TaskAction
	fun failOnFailures() {
		val gatherer = TestReportGatherer(Test::class.java)
		val violations = testResultDirs.files.flatMap {
			// reportOn above added the binary folder, so the XMLs are one up
			gatherer.findViolations(File(it, ".."))
		}
		val errors = (violations.groupBy { it.severity })[SEVERITY.ERROR]
		if (errors.orEmpty().isNotEmpty()) {
			val report = File(destinationDir, "index.html").toURI()
			throw GradleException("There were ${errors!!.size} failing tests. See the report at: ${report}")
		}
	}
}
