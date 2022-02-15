plugins {
	kotlin
	`java-library`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-agp")
description = "AGP Compatibility: Support classes for users of Android Gradle Plugin."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp70x)

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
