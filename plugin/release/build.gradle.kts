plugins {
	kotlin
	id("java-gradle-plugin")
	`java-test-fixtures`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-release")
description = "Release Convention Plugin: Gradle Plugin to handle conventional releasing."

dependencies {
	implementation(gradleApiWithoutKotlin())
	implementation(projects.plugin.base)
	implementation(projects.plugin.versioning) // TODO decouple
	compileOnly(libs.android.gradle)

	// This plugin is part of the net.twisterrob.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
	testImplementation(libs.junit.pioneer)
}

disableGradlePluginValidation()
