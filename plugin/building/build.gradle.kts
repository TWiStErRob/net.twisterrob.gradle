plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
	id("org.gradle.java-test-fixtures")
}

base.archivesName.set("twister-convention-building")
description = "Build Convention Plugin: Gradle Plugin to handle conventional builds."

gradlePlugin {
	disableGradlePluginValidation()
	plugins {
		// Not created, internal plugins only used by class reference.
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	api(projects.plugin.base)
	implementation(projects.compat.agp)
	implementation(projects.plugin.versioning) // TODO decouple
	compileOnly(libs.android.gradle)
	// Need com.android.xml.AndroidXPathFactory for AndroidInstallRunnerTask.Companion.getMainActivity$plugin.
	compileOnly(libs.android.tools.common)
	compileOnly(libs.annotations.jetbrains)

	// This plugin is part of the net.twisterrob.gradle.plugin.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
	testImplementation(testFixtures(projects.plugin.versioning))
	// AndroidInstallRunnerTaskTest calls production code directly, so need com.android.xml.AndroidXPathFactory.
	testRuntimeOnly(libs.android.tools.common)
}
