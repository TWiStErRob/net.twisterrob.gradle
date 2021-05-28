plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesBaseName = "twister-quality"
description = "Quality: All quality plugins bundled in one."

dependencies {
	api(project(":common"))
	api(project(":checkstyle"))
	api(project(":pmd"))

	compileOnly(Libs.Android.plugin)
//	compileOnly ("de.aaschmid:gradle-cpd-plugin:1.0")
	api(Libs.violations)

	testImplementation(project(":test"))
	testImplementation(project(":test:internal"))
	testRuntimeOnly(Libs.Android.plugin)

	testImplementation(testFixtures(project(":pmd")))
}

listOf(":test", ":checkstyle").forEach(project::pullTestResourcesFrom)
