plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-convention-signing")
description = "Signing Convention Plugin: Gradle Plugin to apply Android Signing conventions."

dependencies {
	implementation(gradleApiWithoutKotlin())
	api(projects.plugin.base)
	compileOnly(libs.android.gradle)

	// This plugin is part of the net.twisterrob.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
}

disableGradlePluginValidation()
