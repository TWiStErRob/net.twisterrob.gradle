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
	implementation(projects.compat.agp81x)
	implementation(projects.compat.agp82x)
	implementation(projects.compat.agp83x)
	implementation(projects.compat.agp84x)
	implementation(projects.compat.agp85x)
	implementation(projects.compat.agp86x)
	implementation(projects.compat.agp87x)
	implementation(projects.compat.agp88x)
	implementation(projects.compat.agp89x)
	implementation(projects.compat.agp813x)
	implementation(projects.compat.agp90x)

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
