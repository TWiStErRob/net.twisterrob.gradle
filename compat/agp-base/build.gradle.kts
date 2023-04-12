import net.twisterrob.gradle.build.dependencies.gradleApiWithoutKotlin

plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-agp-base")
description = "AGP Compatibility: Support classes for compatibility layers of Android Gradle Plugin."

dependencies {
	implementation(gradleApiWithoutKotlin())

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
