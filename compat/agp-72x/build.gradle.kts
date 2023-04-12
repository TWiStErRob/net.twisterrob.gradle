import net.twisterrob.gradle.build.dependencies.gradleApiWithoutKotlin

plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-agp-7.2.x")
description = "AGP Compatibility 7.2.x: Compatibility layer for Android Gradle Plugin 7.2.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v72x)
}
