plugins {
	id("org.gradle.java-library")
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.publishing")
	id("net.twisterrob.gradle.build.detekt")
}

base.archivesName.set("twister-compat-agp-base")
description = "AGP Compatibility: Support classes for compatibility layers of Android Gradle Plugin."

dependencies {
	implementation(gradleApiWithoutKotlin())

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
