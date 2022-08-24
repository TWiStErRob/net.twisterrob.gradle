package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.TaskCreationConfiguration
import net.twisterrob.gradle.common.wasLaunchedOnly
import net.twisterrob.gradle.compat.setRequired
import net.twisterrob.gradle.quality.gather.TestReportGatherer
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.kotlin.dsl.withType
import org.gradle.util.GradleVersion
import se.bjurr.violations.lib.model.SEVERITY
import java.io.File

open class GlobalTestFinalizerTask : TestReport() {

	init {
		destinationDirCompat = project.buildDir.resolve("reports/tests/allTests")
	}

	@TaskAction
	fun failOnFailures() {
		val gatherer = TestReportGatherer(Test::class.java)
		val violations = testResultsCompat.files.flatMap { resultDir ->
			// reportOn above added the binary folder, so the XMLs are one up
			val xmlDir = resultDir.resolve("..")
			if (!xmlDir.exists()) {
				// Info only because not every module will have tests (e.g. grouping modules).
				logger.info("Skipping $resultDir, because it doesn't exist")
				return@flatMap emptyList()
			} else {
				gatherer.findViolations(xmlDir)
			}
		}
		val errors = violations.filter { it.severity == SEVERITY.ERROR }
		if (errors.isNotEmpty()) {
			val report = destinationDirCompat.resolve("index.html").toURI()
			throw GradleException("There were ${errors.size} failing tests. See the report at: ${report}")
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
			// Detach the result directories to prevent creation on dependsOn relationships.
			// When simply using reportOn(tests) or reportOn(tasks.map { it.binaryResultDirectory }) task dependencies would be created.
			task.testResultsCompat = task.project.files(tests.map { it.binaryResultsDirectoryCompat })
			// Force executing tests (if they're in the task graph), before reporting on them.
			task.mustRunAfter(tests)
		}
	}
}

private val Test.binaryResultsDirectoryCompat: Any?
	get() =
		if (GradleVersion.current().baseVersion < GradleVersion.version("5.6")) {
			@Suppress("DEPRECATION" /* Gradle 7, to be removed in Gradle 8 */)
			binResultsDir
		} else {
			// Need to create an indirection with a provider to keep it lazy,
			// but also detach from the DirectoryProperty, which references its owning task.
			project.provider { binaryResultsDirectory.get() }
		}

private var TestReport.destinationDirCompat: File
	get() =
		if (GradleVersion.current().baseVersion < GradleVersion.version("7.4")) {
			this.destinationDir
		} else {
			this.destinationDirectory.get().asFile
		}
	set(value) {
		if (GradleVersion.current().baseVersion < GradleVersion.version("7.4")) {
			this.destinationDir = value
		} else {
			this.destinationDirectory.set(value)
		}
	}

private var TestReport.testResultsCompat: FileCollection
	get() =
		if (GradleVersion.current().baseVersion < GradleVersion.version("7.4")) {
			this.testResultDirs
		} else {
			this.testResults
		}
	set(value) {
		if (GradleVersion.current().baseVersion < GradleVersion.version("7.4")) {
			@Suppress("DEPRECATION" /* Gradle 7.4, to be removed in Gradle 8 */)
			this.reportOn(value)
		} else {
			this.testResults.from(value)
		}
	}
