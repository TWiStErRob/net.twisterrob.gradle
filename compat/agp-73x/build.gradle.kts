plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-agp-7.3.x")
description = "AGP Compatibility 7.3.x: Compatibility layer for Android Gradle Plugin 7.3.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v73x)
}
