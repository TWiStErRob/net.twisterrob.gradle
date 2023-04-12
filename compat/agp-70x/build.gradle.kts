plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-agp-7.0.x")
description = "AGP Compatibility 7.0.x: Compatibility layer for Android Gradle Plugin 7.0.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v70x)
}
