plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-gradle")
description = "Gradle Compatibility: Support methods for compatibility with all supported Gradle versions."

dependencies {
	implementation(gradleApiWithoutKotlin())

	testImplementation(projects.test.internal)
}
