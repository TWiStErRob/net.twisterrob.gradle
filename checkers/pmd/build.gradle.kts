plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesBaseName = "twister-quality-pmd"
description = "PMD: PMD quality setup plugin for Gradle."

dependencies {
	api(project(":common"))

	compileOnly(Libs.Android.plugin)

	testImplementation(project(":test"))
	testImplementation(project(":test:internal"))
}

pullTestResourcesFrom(":test")
