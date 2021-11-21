plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-plugins-compat-4.0")
description = "Compatibility 4.0: Compatibility layer for Android Gradle Plugin 4.0.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v40x)
}
