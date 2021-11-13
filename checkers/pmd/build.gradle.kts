plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
	`java-test-fixtures`
	id("net.twisterrob.gradle.build.publishing")
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

	compileOnly(Libs.Android.plugin)

	testImplementation(projects.test.internal)

	testFixturesImplementation(projects.test.internal)
}

pullTestResourcesFrom(projects.test)
