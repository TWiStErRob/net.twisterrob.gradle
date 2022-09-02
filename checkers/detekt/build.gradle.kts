plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
	`java-test-fixtures`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-quality-detekt")
description = "Detekt: Detekt quality setup plugin for Gradle."

gradlePlugin {
	plugins {
		create("net.twisterrob.detekt") {
			id = "net.twisterrob.detekt"
			implementationClass = "net.twisterrob.gradle.detekt.DetektPlugin"
		}
	}
}

dependencies {
	api(projects.common)

	compileOnly(libs.android.gradle)
	compileOnly(libs.detekt.gradle)

	testImplementation(projects.test.internal)
	testImplementation(projects.compat.agpBase)

	testFixturesImplementation(projects.test.internal)

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.detekt.gradle)
	// For browsing code only. It has to be runtimeOnly, otherwise the transitive runtime dependencies don't resolve.
	testRuntimeOnly(libs.detekt.cli)
}

pullTestResourcesFrom(projects.test)
