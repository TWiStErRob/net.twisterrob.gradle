plugins {
	id("org.gradle.java-library")
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.publishing")
	id("net.twisterrob.gradle.build.detekt")
}

base.archivesName.set("twister-compat-agp-4.2.x")
description = "AGP Compatibility 4.2.x: Compatibility layer for Android Gradle Plugin 4.2.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v42x)

	compileOnly(libs.violations)

	implementation(projects.compat.agpBase)
	implementation(projects.compat.gradle)
}
