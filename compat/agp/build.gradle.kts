plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp"
description = "AGP Compatibility: Support classes for users of Android Gradle Plugin."

dependencies {
	implementation(gradleApi())
	compileOnly(libs.android.gradle)
	api(projects.compat.agpBase)
	implementation(projects.compat.agp813x)
	implementation(projects.compat.agp90x)
	implementation(projects.compat.agp91x)
	implementation(projects.compat.agp92x)
	implementation(projects.compat.agp93x)

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
