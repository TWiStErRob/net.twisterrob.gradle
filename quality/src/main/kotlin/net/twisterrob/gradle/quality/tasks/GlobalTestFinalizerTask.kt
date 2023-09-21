package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.TaskCreationConfiguration
import net.twisterrob.gradle.common.wasLaunchedOnly
import net.twisterrob.gradle.quality.gather.TestReportGatherer
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.kotlin.dsl.withType
import org.gradle.util.GradleVersion
import org.gradle.work.DisableCachingByDefault
import se.bjurr.violations.lib.model.SEVERITY

@DisableCachingByDefault(because = "Base class is not cacheable yet. (Gradle 8.0)")
abstract class GlobalTestFinalizerTask : TestReport() {

	/**
	 * Need to save the value to a field, so that we can query the provider in @TaskAction.
	 * Without this, usages (`.get()`) fail with the following exception:
	 * > Querying the mapped value of task ':testReport' property 'destinationDirectory' before task ':testReport' has completed is not supported
	 */
	@get:Internal
	internal val output: Provider<Directory> = project.layout.buildDirectory.dir("reports/tests/allTests")

	init {
		destinationDirCompat = output
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
			val report = output.map { it.file("index.html") }.get().asFile.toURI()
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
					test.reports.junitXml.required.set(true)
					// Let the tests/build finish, to get a final "all" report.
					test.ignoreFailures = !test.wasLaunchedOnly
				}
			// Detach the result directories to prevent creation on dependsOn relationships.
			// When simply using reportOn(tests) or reportOn(tasks.map { it.binaryResultDirectory }) task dependencies would be created.
			task.testResultsCompat = task.project.files(tests.map(Test::detachBinaryResultsDirectory))
			// Force executing tests (if they're in the task graph), before reporting on them.
			task.mustRunAfter(tests)
		}
	}
}

private val Test.detachBinaryResultsDirectory: Provider<Directory>
	// Need to create an indirection with a provider to keep it lazy,
	// but also detach from the DirectoryProperty, which references its owning task.
	get() = project.provider { this.binaryResultsDirectory.get() }

/**
 * Note: this is no ordinary DirectoryProperty,
 * `destinationDirCompat.set(...)` won't work, use `destinationDirCompat::set` instead.
 */
@Suppress("UseIfInsteadOfWhen") // Preparing for future new version ranges.
private var TestReport.destinationDirCompat: Provider<Directory>
	get() =
		when {
			GradleVersion.version("7.4") <= GradleVersion.current().baseVersion -> {
				this.destinationDirectory
			}
			else -> {
				@Suppress("DEPRECATION" /* Gradle 7.6, to be removed in Gradle 9 */)
				val destinationDir = this.destinationDir
				this.project.objects.directoryProperty().fileValue(destinationDir)
			}
		}
	set(value) {
		when {
			GradleVersion.version("7.4") <= GradleVersion.current().baseVersion -> {
				this.destinationDirectory.set(value)
			}
			else -> {
				@Suppress("DEPRECATION" /* Gradle 7.6, to be removed in Gradle 9 */)
				this.destinationDir = value.get().asFile
			}
		}
	}

@Suppress("UseIfInsteadOfWhen") // Preparing for future new version ranges.
private var TestReport.testResultsCompat: FileCollection
	get() =
		when {
			GradleVersion.version("7.4") <= GradleVersion.current().baseVersion -> {
				this.testResults
			}
			else -> {
				@Suppress("DEPRECATION" /* Gradle 7.6, removed in Gradle 8 */)
				this.testResultDirs
			}
		}
	set(value) {
		when {
			GradleVersion.version("7.4") <= GradleVersion.current().baseVersion -> {
				this.testResults.from(value)
			}
			else -> {
				@Suppress("DEPRECATION" /* Gradle 7.4, to be removed in Gradle 9 */)
				this.reportOn(value)
			}
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
