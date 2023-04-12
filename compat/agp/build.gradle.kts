plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-agp")
description = "AGP Compatibility: Support classes for users of Android Gradle Plugin."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp70x)
	implementation(projects.compat.agp71x)
	implementation(projects.compat.agp72x)
	implementation(projects.compat.agp73x)
	implementation(projects.compat.agp74x)
	implementation(projects.compat.agp80x)
	implementation(projects.compat.agp81x)

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
