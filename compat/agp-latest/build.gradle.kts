plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-agp-latest")
description = "AGP Compatibility Latest: Compatibility layer for Android Gradle Plugin latest version used by Gradle Quality plugins."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle)
}
