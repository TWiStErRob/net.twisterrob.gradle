plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-plugins-compat-4.1")
description = "Compatibility 4.1: Compatibility layer for Android Gradle Plugin 4.1.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v41x)
}
