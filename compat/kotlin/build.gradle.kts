plugins {
	id("org.gradle.java-library")
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-kotlin")
description = "Kotlin Compatibility: Support methods for compatibility with all supported Kotlin versions."

dependencies {
	testImplementation(projects.test.internal)
}
