plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-kotlin-base")
description = "Kotlin Compatibility: Support classes for compatibility layers of Kotlin and Kotlin Gradle Plugin."

dependencies {
	testImplementation(projects.test.internal)
}
