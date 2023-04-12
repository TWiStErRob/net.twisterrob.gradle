import net.twisterrob.gradle.build.testing.pullTestResourcesFrom

plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
	id("org.gradle.java-test-fixtures")
}

base.archivesName.set("twister-quality")
description = "Quality: All quality plugins bundled in one."

gradlePlugin {
	@Suppress("UnstableApiUsage", "StringLiteralDuplication")
	plugins {
		create("quality") {
			id = "net.twisterrob.gradle.plugin.quality"
			displayName = "Gradle Quality Plugins"
			description = """
				Sane defaults for Checkstyle, PMD, Lint, Test reports which make multi-module Gradle project CI easier.
			""".trimIndent()
			tags.set(setOf("multi-module", "android", "reporting", "quality", "static-checks", "CI", "checkstyle", "pmd"))
			implementationClass = "net.twisterrob.gradle.quality.QualityPlugin"
			deprecateId(project, "net.twisterrob.quality")
		}
	}
}

dependencies {
	api(projects.common)
	api(projects.checkstyle)
	api(projects.pmd)

	implementation(projects.compat.gradle)
	implementation(projects.compat.agpBase)
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
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}

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
		projects.test.internal.runtime,
		projects.test.integration,
	).forEach { project ->
		dependsOn(project.dependencyProject.tasks.named("test"))
	}
	dependsOn(projects.compat.dependencyProject.tasks.named("tests"))
}

pullTestResourcesFrom(projects.test)
