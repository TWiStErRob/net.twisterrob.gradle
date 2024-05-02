plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-8.5.x"
description = "AGP Compatibility 8.5.x: Compatibility layer for Android Gradle Plugin 8.5.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v85x)
	implementation(projects.compat.agpBase)
}
