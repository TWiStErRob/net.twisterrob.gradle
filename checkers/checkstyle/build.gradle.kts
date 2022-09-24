plugins {
	id("org.gradle.java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm")
	id("org.gradle.java-test-fixtures")
	id("net.twisterrob.gradle.build.publishing")
	id("net.twisterrob.gradle.build.detekt")
}

base.archivesName.set("twister-quality-checkstyle")
description = "Checkstyle: Checkstyle quality setup plugin for Gradle."

gradlePlugin {
	plugins {
		create("net.twisterrob.checkstyle") {
			id = "net.twisterrob.checkstyle"
			implementationClass = "net.twisterrob.gradle.checkstyle.CheckStylePlugin"
		}
	}
}

dependencies {
	api(projects.common)

	compileOnly(libs.android.gradle)

	testImplementation(projects.test.internal)
	testImplementation(projects.compat.agpBase)

	testFixturesImplementation(projects.test.internal)
}

pullTestResourcesFrom(projects.test)
