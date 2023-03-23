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
import org.gradle.api.tasks.testing.AbstractTestTask
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
				emptyList()
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
			// No pre-configuration, all config is lazy.
		}

		override fun configure(task: GlobalTestFinalizerTask) {
			val tests = task.project.allprojects
				.flatMap { it.tasks.withType<Test>() } // Forces to create the tasks.
				.onEach { test ->
					// Make sure we have XML output, otherwise can't figure out if test failed.
					test.reports.junitXml.setRequired(true)
					// Let the tests/build finish, to get a final "all" report.
					test.ignoreFailures = !test.wasLaunchedOnly
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
	get() = // STOPSHIP inline?
		// Need to create an indirection with a provider to keep it lazy,
		// but also detach from the DirectoryProperty, which references its owning task.
		project.provider { this.binaryResultsDirectory.get() }

private var TestReport.destinationDirCompat: File
	get() = // STOPSHIP did the new version exist in 7.0?
		if (GradleVersion.current().baseVersion < GradleVersion.version("7.4")) {
			@Suppress("DEPRECATION" /* Gradle 7.6, to be removed in Gradle 9 */)
			this.destinationDir
		} else {
			this.destinationDirectory.get().asFile
		}
	set(value) {
		if (GradleVersion.current().baseVersion < GradleVersion.version("7.4")) {
			@Suppress("DEPRECATION" /* Gradle 7.6, to be removed in Gradle 8 */)
			this.destinationDir = value
		} else {
			this.destinationDirectory.set(value)
		}
	}

private var TestReport.testResultsCompat: FileCollection
	get() = // STOPSHIP did the new version exist in 7.0?
		if (GradleVersion.current().baseVersion < GradleVersion.version("7.4")) {
			@Suppress("DEPRECATION")
			this.testResultDirs
		} else {
			this.testResults
		}
	set(value) {
		if (GradleVersion.current().baseVersion < GradleVersion.version("7.4")) {
			@Suppress("DEPRECATION" /* Gradle 7.4, to be removed in Gradle 9 */)
			this.reportOn(value)
		} else {
			this.testResults.from(value)
		}
	}

/**
 * Polyfill as reflective call, as this method was...
 *  * [Added in Gradle 1.4](https://github.com/gradle/gradle/commit/c7dd16ecfbb438ce153153efff008ee83764d394)
 *  * [Marked for replacement in Gradle 7.4](https://github.com/gradle/gradle/pull/19732)
 *  * [Deprecated in Gradle 7.6](https://docs.gradle.org/8.0-rc-1/userguide/upgrading_version_7.html#replacement_methods_in_org_gradle_api_tasks_testing_testreport)
 *    [PR](https://github.com/gradle/gradle/pull/21371)
 *  * [Removed in Gradle 8.0](https://github.com/gradle/gradle/issues/23390)
 *
 * @see TestReport.getTestResults
 */
@Deprecated(
	message = "Replaced with TestReport.testResults.",
	replaceWith = ReplaceWith("testResults.from(value)")
)
private val TestReport.testResultDirs: FileCollection
	get() {
		val method = TestReport::class.java.getDeclaredMethod("getTestResultDirs")
		return method(this) as FileCollection
	}
