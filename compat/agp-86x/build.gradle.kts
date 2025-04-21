plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-8.6.x"
description = "AGP Compatibility 8.6.x: Compatibility layer for Android Gradle Plugin 8.6.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v86x)
	implementation(projects.compat.agpBase)
}
