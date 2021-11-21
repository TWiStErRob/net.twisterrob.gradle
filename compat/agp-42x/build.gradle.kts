plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-agp-4.2.x")
description = "Compatibility 4.2.x: Compatibility layer for Android Gradle Plugin 4.2.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v42x)

	implementation(projects.compat.agp)
}
