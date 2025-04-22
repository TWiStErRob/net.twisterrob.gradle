package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.TaskCreationConfiguration
import net.twisterrob.gradle.common.wasLaunchedOnly
import net.twisterrob.gradle.quality.gather.TestReportGatherer
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.kotlin.dsl.withType
import org.gradle.work.DisableCachingByDefault
import se.bjurr.violations.lib.model.SEVERITY

@DisableCachingByDefault(because = "Base class is not cacheable yet. (Gradle 8.0)")
@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class GlobalTestFinalizerTask : TestReport() {

	init {
		destinationDirectory.convention(project.layout.buildDirectory.dir("reports/tests/allTests"))
	}

	@TaskAction
	fun failOnFailures() {
		val gatherer = TestReportGatherer(Test::class.java)
		val violations = testResults.files.flatMap { resultDir ->
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
			val report = destinationDirectory.map { it.file("index.html") }.get().asFile.toURI()
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
			task.testResults.from(task.project.files(tests.map(Test::detachBinaryResultsDirectory)))
			// Force executing tests (if they're in the task graph), before reporting on them.
			task.mustRunAfter(tests)
		}
	}
}

private val Test.detachBinaryResultsDirectory: Provider<Directory>
	// Need to create an indirection with a provider to keep it lazy,
	// but also detach from the DirectoryProperty, which references its owning task.
	get() = project.provider { this.binaryResultsDirectory.get() }
