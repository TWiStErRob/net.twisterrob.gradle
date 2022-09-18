plugins {
	id("org.gradle.java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm")
	id("org.gradle.java-test-fixtures")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-building")
description = "Build Convention Plugin: Gradle Plugin to handle conventional builds."

dependencies {
	implementation(gradleApiWithoutKotlin())
	api(projects.plugin.base)
	implementation(projects.compat.agpBase)
	implementation(projects.plugin.versioning) // TODO decouple
	compileOnly(libs.android.gradle)
	// Need com.android.xml.AndroidXPathFactory for AndroidInstallRunnerTask.Companion.getMainActivity$plugin.
	compileOnly(libs.android.tools.common)
	compileOnly(libs.annotations.jetbrains)

	// This plugin is part of the net.twisterrob.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
	testImplementation(testFixtures(projects.plugin.versioning))
	// AndroidInstallRunnerTaskTest calls production code directly, so need com.android.xml.AndroidXPathFactory.
	testImplementation(libs.android.tools.common)
}

disableGradlePluginValidation()
