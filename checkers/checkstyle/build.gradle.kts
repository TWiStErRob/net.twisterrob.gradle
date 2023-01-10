plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
	id("org.gradle.java-test-fixtures")
}

base.archivesName.set("twister-quality-checkstyle")
description = "Checkstyle: Checkstyle quality setup plugin for Gradle."

gradlePlugin {
	@Suppress("UnstableApiUsage", "StringLiteralDuplication")
	plugins {
		create("net.twisterrob.checkstyle") {
			id = "net.twisterrob.checkstyle"
			displayName = "Checkstyle Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Checkstyle.
				
				Features:
				 * Automatic setup of Checkstyle configuration
				 * Conventional location: config/checkstyle/checkstyle.xml
				 * Cleaner reporting by removing redundant info from reports.
			""".trimIndent()
			tags.set(setOf("conventions", "checkstyle"))
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
