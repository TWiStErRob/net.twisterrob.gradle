plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-agp-8.2.x")
description = "AGP Compatibility 8.2.x: Compatibility layer for Android Gradle Plugin 8.2.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v82x)
	implementation(projects.compat.agpBase)
}
