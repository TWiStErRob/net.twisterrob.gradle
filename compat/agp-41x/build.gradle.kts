plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-agp-4.1.x")
description = "Compatibility 4.1.x: Compatibility layer for Android Gradle Plugin 4.1.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v41x)
}
