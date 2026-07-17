plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-9.1.x"
description = "AGP Compatibility 9.1.x: Compatibility layer for Android Gradle Plugin 9.1.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v91x)
	implementation(projects.compat.agpBase)
}
