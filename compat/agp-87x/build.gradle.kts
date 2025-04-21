plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-8.7.x"
description = "AGP Compatibility 8.7.x: Compatibility layer for Android Gradle Plugin 8.7.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v87x)
	implementation(projects.compat.agpBase)
}
