package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.TaskCreationConfiguration
import net.twisterrob.gradle.common.wasLaunchedOnly
import net.twisterrob.gradle.compat.setRequired
import net.twisterrob.gradle.quality.gather.TestReportGatherer
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.kotlin.dsl.withType
import se.bjurr.violations.lib.model.SEVERITY
import java.io.File

open class GlobalTestFinalizerTask : TestReport() {

	init {
		destinationDir = project.buildDir.resolve("reports/tests/allTests")
	}

	@TaskAction
	fun failOnFailures() {
		val gatherer = TestReportGatherer(Test::class.java)
		val violations = testResultDirs.files.flatMap {
			// reportOn above added the binary folder, so the XMLs are one up
			gatherer.findViolations(File(it, ".."))
		}
		val errors = (violations.groupBy { it.severity })[SEVERITY.ERROR]
		if (errors.orEmpty().isNotEmpty()) {
			val report = destinationDir.resolve("index.html").toURI()
			throw GradleException("There were ${errors!!.size} failing tests. See the report at: ${report}")
		}
	}

	class Creator : TaskCreationConfiguration<GlobalTestFinalizerTask> {
		override fun preConfigure(project: Project, taskProvider: TaskProvider<GlobalTestFinalizerTask>) {
		}

		override fun configure(task: GlobalTestFinalizerTask) {
			val tests = task.project.allprojects
				.flatMap { it.tasks.withType<Test>() } // Forces to create the tasks.
				.onEach {
					// Make sure we have XML output, otherwise can't figure out if test failed.
					it.reports.junitXml.setRequired(true)
					// Let the tests/build finish, to get a final "all" report.
					it.ignoreFailures = !it.wasLaunchedOnly
				}
			// Detach the result directories, simply using reportOn(tests) or the providers, task dependencies will be created.
			task.reportOn(tests.map { it.binaryResultsDirectory.get() })
			// Force executing tests (if they're in the task graph), before reporting on them.
			task.mustRunAfter(tests)
		}
	}
}
