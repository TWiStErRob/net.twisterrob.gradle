plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
	`java-test-fixtures`
	id("net.twisterrob.gradle.build.publishing")
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
	api(project(":common"))

	compileOnly(Libs.Android.plugin)

	testImplementation(project(":test:internal"))

	testFixturesImplementation(project(":test:internal"))
}

pullTestResourcesFrom(":test")
