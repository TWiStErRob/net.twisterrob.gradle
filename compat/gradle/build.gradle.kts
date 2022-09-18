plugins {
	id("org.gradle.java-library")
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-gradle")
description = "Gradle Compatibility: Support methods for compatibility with all supported Gradle versions."

dependencies {
	implementation(gradleApiWithoutKotlin())

	testImplementation(projects.test.internal)
}
