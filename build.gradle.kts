plugins {
	id("net.twisterrob.gradle.build.module.root")
}

description = "Plugins for Gradle that support Android flavors."

// buildscript { enableDependencyLocking(project) } // STOPSHIP Cannot import for some reason.

subprojects {
	// Extension with name 'libs' does not exist. Currently registered extension names: [ext, kotlin, kotlinTestRegistry, base, defaultArtifacts, sourceSets, reporting, java, javaToolchains, testing]
	// Needs to be called different from libs,
	// because com.android.tools.idea.gradle.dsl.model.ext.PropertyUtil.followElement
	// from idea-2021.1.3\plugins\android-gradle-dsl\lib\android-gradle-dsl-impl.jar
	// runs into an infinite loop on it.
	// TODEL Affects anything with Android Plugin < 2020.3.1 (i.e. AS 4.x, and IJ <2021.3)
	val deps = rootProject.libs

	plugins.withId("org.jetbrains.kotlin.jvm") {
		dependencies {
			// Make sure we don't have many versions of Kotlin lying around.
			add("compileOnly", enforcedPlatform(deps.kotlin.bom))
			add("testCompileOnly", enforcedPlatform(deps.kotlin.bom))
			plugins.withId("org.gradle.java-test-fixtures") {
				add("testFixturesCompileOnly", enforcedPlatform(deps.kotlin.bom))
			}

			add("compileOnly", deps.kotlin.dsl) {
				isTransitive = false // make sure to not pull in kotlin-compiler-embeddable
			}
			add("api", deps.kotlin.stdlib)
			add("api", deps.kotlin.stdlib.jdk8)
			add("api", deps.kotlin.reflect)
		}
	}
}

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
