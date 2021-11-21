plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-agp-3.3.x")
description = "Compatibility 3.3.x: Compatibility layer for Android Gradle Plugin 3.3.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v33x)
}
