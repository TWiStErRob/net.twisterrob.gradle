plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
	id("org.gradle.java-test-fixtures")
}

base.archivesName.set("twister-quality-pmd")
description = "PMD: PMD quality setup plugin for Gradle."

gradlePlugin {
	plugins {
		create("net.twisterrob.pmd") {
			id = "net.twisterrob.pmd"
			implementationClass = "net.twisterrob.gradle.pmd.PmdPlugin"
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
