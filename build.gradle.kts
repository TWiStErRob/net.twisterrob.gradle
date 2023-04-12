plugins {
	id("net.twisterrob.gradle.build.module.root")
}

description = "Plugins for Gradle that support Android flavors."

project.tasks.register<TestReport>("testReport") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Run and report on all tests in the project. Add `-x test` to just generate report."
	destinationDirectory.set(file("${buildDir}/reports/tests/all"))

	val tests = subprojects
		.flatMap { it.tasks.withType(Test::class) } // Forces to create the tasks.
		.onEach { it.ignoreFailures = true } // Let the tests finish, to get a final "all" report.
	// Detach (.get()) the result directories,
	// simply using reportOn(tests) or the binaryResultsDirectory providers, task dependencies would be created.
	testResults.from(tests.map { it.binaryResultsDirectory.get() })
	// Force executing tests (if they're in the task graph), before reporting on them.
	mustRunAfter(tests)

	doLast {
		val reportFile = destinationDirectory.file("index.html").get().asFile
		val failureRegex = Regex("""(?s).*<div class="infoBox" id="failures">\s*<div class="counter">(\d+)<\/div>.*""")
		val failureMatch = failureRegex.matchEntire(reportFile.readText())
		val reportPath = reportFile.toURI().toString().replace("file:/([A-Z])".toRegex(), "file:///\$1")
		if (failureMatch == null) {
			throw GradleException("Cannot determine if the tests failed. See the report at: ${reportPath}")
		} else {
			val failCount = failureMatch.groups[1]!!.value
			if (failCount != "0") {
				throw GradleException("There were ${failCount} failing tests. See the report at: ${reportPath}")
			}
		}
	}
}
