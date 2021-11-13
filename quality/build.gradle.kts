plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
	`java-test-fixtures`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-quality")
description = "Quality: All quality plugins bundled in one."

gradlePlugin {
	plugins {
		create("net.twisterrob.quality") {
			id = "net.twisterrob.quality"
			implementationClass = "net.twisterrob.gradle.quality.QualityPlugin"
		}
	}
}

dependencies {
	api(projects.common)
	api(projects.checkstyle)
	api(projects.pmd)

	compileOnly(Libs.Android.plugin)
//	compileOnly ("de.aaschmid:gradle-cpd-plugin:1.0")
	api(Libs.violations)

	testImplementation(projects.test.internal)
	testRuntimeOnly(Libs.Android.plugin)

	testImplementation(testFixtures(projects.pmd))
	testImplementation(testFixtures(projects.checkstyle))
}

pullTestResourcesFrom(":test")
