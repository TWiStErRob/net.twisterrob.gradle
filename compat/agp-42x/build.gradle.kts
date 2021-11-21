plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-plugins-compat-4.2")
description = "Compatibility 4.2: Compatibility layer for Android Gradle Plugin 4.2.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v42x)
}
