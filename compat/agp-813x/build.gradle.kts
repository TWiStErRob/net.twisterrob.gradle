plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-8.13.x"
description = "AGP Compatibility 8.13.x: Compatibility layer for Android Gradle Plugin 8.13.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v813x)
	implementation(projects.compat.agpBase)
}
