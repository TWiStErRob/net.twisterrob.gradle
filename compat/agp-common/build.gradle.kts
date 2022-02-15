plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-agp-common")
description = "AGP Compatibility: Support classes for compatibility layers of Android Gradle Plugin."

dependencies {
	implementation(gradleApiWithoutKotlin())

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
