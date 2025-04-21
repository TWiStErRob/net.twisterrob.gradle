import net.twisterrob.gradle.build.testing.configureVerboseReportsForGithubActions

plugins {
	id("org.gradle.java-base")
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
}

if (providers.gradleProperty("net.twisterrob.gradle.build.verboseReports").map(String::toBoolean).get()) {
	tasks.withType<Test>().configureEach {
		configureVerboseReportsForGithubActions()
	}
}

tasks.named<Test>("test") {
	testLogging.events("passed", "skipped", "failed")
}

tasks.withType<Test>().configureEach {
	javaLauncher = javaToolchains.launcherFor {
		languageVersion = providers.gradleProperty("net.twisterrob.test.gradle.javaVersion")
			.map(JavaLanguageVersion::of)
	}
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
		.associateWith(providers::gradleProperty)
		.toMutableMap()
	if (System.getProperties().containsKey("idea.active")) {
		logger.debug("Keeping folder contents after test run from IDEA")
		// see net.twisterrob.gradle.test.GradleRunnerRule
		properties["net.twisterrob.gradle.runner.clearAfterSuccess"] = provider { "false" }
		properties["net.twisterrob.gradle.runner.clearAfterFailure"] = provider { "false" }
	}
	val tmpdir = providers.gradleProperty("net.twisterrob.test.java.io.tmpdir").get()
	if (tmpdir.isNotEmpty()) {
		// Used in GradleTestKitDirRelocator.
		properties["java.io.tmpdir"] = provider { tmpdir }
	}
	// TODEL https://github.com/gradle/gradle/issues/861
	properties.forEach { (name, value) -> inputs.property(name, value.get()) }
	properties.forEach { (name, value) -> jvmArgs("-D${name}=${value.get()}") }
}
