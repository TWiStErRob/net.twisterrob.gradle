plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-8.11.x"
description = "AGP Compatibility 8.11.x: Compatibility layer for Android Gradle Plugin 8.11.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v811x)
	implementation(projects.compat.agpBase)
}
