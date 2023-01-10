plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-convention-settings")
description = "Settings Convention Plugin: Gradle Plugin to apply in settings.gradle files."

gradlePlugin {
	@Suppress("UnstableApiUsage", "StringLiteralDuplication")
	plugins {
		create("net.twisterrob.settings") {
			id = "net.twisterrob.settings"
			displayName = "Gradle Settings Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Gradle Settings files.
				
				Features:
				 * Exposes utility functions used in many projects:
				   * `enableFeaturePreviewQuietly`
				   * `doNotNagAbout`
				   * `slug`
				 * No logic yet.
			""".trimIndent()
			tags.set(setOf("utilities", "settings", "logging"))
			implementationClass = "net.twisterrob.gradle.settings.SettingsPlugin"
		}
	}
}

kotlin.sourceSets.named("main").configure {
	// Create separate source-set for sharing code between the project and its build, see /gradle/plugins for more info.
	// Note: the code is housed in this module so that it can be tested appropriately.
	kotlin.srcDir("src/main/kotlin-shared")
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	// Expose some methods to TestKit runtime classpath.
	runtimeOnly(projects.compat.gradle)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
	testImplementation(projects.compat.gradle)
}
