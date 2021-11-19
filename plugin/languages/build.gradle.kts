plugins {
	kotlin
	id("java-gradle-plugin")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-languages")
description = "Languages Convention Plugin: Gradle Plugin to apply Java and Kotlin convetions."

gradlePlugin {
	plugins {
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	implementation(projects.common)
	implementation(projects.plugin.base)
	compileOnly(libs.android.gradle)

	// This plugin is part of the net.twisterrob.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
}
