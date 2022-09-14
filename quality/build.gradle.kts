plugins {
	id("org.jetbrains.kotlin.jvm")
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

	implementation(projects.compat.gradle)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp42x)
	implementation(projects.compat.agpLatest)
	implementation(projects.compat.agp)

	compileOnly(libs.annotations.jetbrains)
	compileOnly(libs.android.gradle)
	// Need com.android.utils.FileUtils for HtmlReportTask.
	compileOnly(libs.android.tools.common)
	api(libs.violations)

	testImplementation(projects.test.internal)
	testRuntimeOnly(libs.android.gradle)

	testImplementation(testFixtures(projects.pmd))
	testImplementation(testFixtures(projects.checkstyle))

	testFixturesImplementation(projects.test.internal)
}

tasks.register("tests") {
	listOf(
		projects.quality,
		projects.common,
		projects.checkstyle,
		projects.pmd,
		projects.test,
		projects.test.internal,
	).forEach {
		dependsOn(it.dependencyProject.tasks.named("test"))
	}
}

pullTestResourcesFrom(projects.test)
