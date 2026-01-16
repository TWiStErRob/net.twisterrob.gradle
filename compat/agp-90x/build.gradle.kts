plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-9.0.x"
description = "AGP Compatibility 9.0.x: Compatibility layer for Android Gradle Plugin 9.0.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v90x)
	implementation(projects.compat.agpBase)
}
