plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-agp-4.0.x")
description = "AGP Compatibility 4.0.x: Compatibility layer for Android Gradle Plugin 4.0.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v40x)
}
