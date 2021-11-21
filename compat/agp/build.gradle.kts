plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-agp")
description = "Android Gradle Plugin Compatibility: Support classes for compatibility layers for Android Gradle Plugin."

dependencies {
	implementation(gradleApiWithoutKotlin())
}
