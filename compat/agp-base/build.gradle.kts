plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-base"
description = "AGP Compatibility: Support classes for compatibility layers of Android Gradle Plugin."

dependencies {
	implementation(gradleApi())

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
