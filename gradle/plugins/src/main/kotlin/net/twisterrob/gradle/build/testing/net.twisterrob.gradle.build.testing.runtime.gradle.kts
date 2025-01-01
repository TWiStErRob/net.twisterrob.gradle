import net.twisterrob.gradle.build.testing.configureVerboseReportsForGithubActions

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
}

if (project.property("net.twisterrob.gradle.build.verboseReports").toString().toBoolean()) {
	tasks.withType<Test>().configureEach {
		configureVerboseReportsForGithubActions()
	}
}

tasks.named<Test>("test") {
	testLogging.events("passed", "skipped", "failed")
}

tasks.withType<Test>().configureEach {
	val propertyNamesToExposeToJUnitTests = listOf(
		// for GradleRunnerRule to use a different Gradle version for tests
		"net.twisterrob.gradle.runner.gradleVersion",
		// for tests to decide dynamically
		"net.twisterrob.test.gradle.javaVersion",
		"net.twisterrob.test.android.pluginVersion",
		"net.twisterrob.test.kotlin.pluginVersion",
		"net.twisterrob.test.android.compileSdkVersion",
		// So that command line gradlew -P...=false works.
		// Will override earlier jvmArgs, if both specified.
		"net.twisterrob.gradle.runner.clearAfterSuccess",
		"net.twisterrob.gradle.runner.clearAfterFailure",
	)
	val properties = propertyNamesToExposeToJUnitTests
		.associateBy({ it }) { project.property(it) }
		.toMutableMap()
	if (System.getProperties().containsKey("idea.active")) {
		logger.debug("Keeping folder contents after test run from IDEA")
		// see net.twisterrob.gradle.test.GradleRunnerRule
		properties["net.twisterrob.gradle.runner.clearAfterSuccess"] = "false"
		properties["net.twisterrob.gradle.runner.clearAfterFailure"] = "false"
	}
	val tmpdir = project.property("net.twisterrob.test.java.io.tmpdir").toString()
	if (tmpdir.isNotEmpty()) {
		// Used in GradleTestKitDirRelocator.
		properties["java.io.tmpdir"] = tmpdir
	}
	// TODEL https://github.com/gradle/gradle/issues/861
	properties.forEach { (name, value) -> inputs.property(name, value) }
	properties.forEach { (name, value) -> value?.let { jvmArgs("-D${name}=${value}") } }
}
