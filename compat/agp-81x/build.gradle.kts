plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-agp-8.1.x")
description = "AGP Compatibility 8.1.x: Compatibility layer for Android Gradle Plugin 8.1.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v81x)
}
