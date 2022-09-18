plugins {
	id("org.gradle.java-library")
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.publishing")
	id("net.twisterrob.gradle.build.detekt")
}

base.archivesName.set("twister-compat-agp-latest")
description = "AGP Compatibility Latest: Compatibility layer for Android Gradle Plugin latest version used by Gradle Quality plugins."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle)
}
