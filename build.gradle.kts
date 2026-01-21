plugins {
	id("net.twisterrob.gradle.build.module.root")
}

description = "Plugins for Gradle that support Android flavors."

project.tasks.register<TestReport>("testReport") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Run and report on all tests in the project. Add `-x test` to just generate report."
	destinationDirectory = project.layout.buildDirectory.dir("reports/tests/all")

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
		val reportHtml = reportFile.readText()
		val failureRegex =
			"""(?s)<div class="infoBox">\s*<div class="counter">(\d+)<\/div>\s*<p>failures</p>""".toRegex()
		val clickableUri = reportFile.toURI().toString().replace("file:/", "file:///")
		if (!failureRegex.containsMatchIn(reportHtml)) {
			throw GradleException("Cannot determine if the tests failed. See the report at: ${clickableUri}")
		} else {
			val failures = failureRegex
				.findAll(reportHtml)
				.map { it.groups[1]?.value?.toInt() }
				.filterNotNull()
				.filter { it != 0 }
				.toList()
			if (failures.isNotEmpty()) {
				throw GradleException("There were ${failures.sum()} failing tests. See the report at: ${clickableUri}")
			}
		}
	}
}

/*
allprojects {
	afterEvaluate {
		tasks.configureEach {
			doLast {
				Thread.sleep((Math.random() * 1500).toLong())
			}
		}
	}
}
*/
