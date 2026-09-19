plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-compat-agp-base"
description = "AGP Compatibility: Support classes for compatibility layers of Android Gradle Plugin."

dependencies {
	implementation(gradleApi())
	// KDoc references com.android.Version, but production code loads it reflectively.
	dokkaClasspath(libs.android.tools.common)

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)
}
