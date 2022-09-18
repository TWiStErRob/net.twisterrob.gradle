plugins {
	id("org.gradle.java-library")
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-compat-kotlin-base")
description = "Kotlin Compatibility: Support classes for compatibility layers of Kotlin and Kotlin Gradle Plugin."

dependencies {
	testImplementation(projects.test.internal)
}
