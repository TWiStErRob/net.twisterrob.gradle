plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-7.1.x"
description = "AGP Compatibility 7.1.x: Compatibility layer for Android Gradle Plugin 7.1.x."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle.v71x)
	implementation(projects.compat.agpBase)
}
