plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-8.4.x"
description = "AGP Compatibility 8.4.x: Compatibility layer for Android Gradle Plugin 8.4.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v84x)
	implementation(projects.compat.agpBase)
}
