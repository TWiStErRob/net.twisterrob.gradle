plugins {
	id("org.gradle.java-library")
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.publishing")
	id("net.twisterrob.gradle.build.detekt")
}

base.archivesName.set("twister-compat-agp")
description = "AGP Compatibility: Support classes for users of Android Gradle Plugin."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp70x)
	implementation(projects.compat.agp71x)

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
