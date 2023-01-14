plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-convention-plugins")
description = "Convention Plugins: Gradle Plugins used by my hobby projects."

gradlePlugin {
	@Suppress("UnstableApiUsage", "StringLiteralDuplication")
	plugins {
		create("androidApp") {
			id = "net.twisterrob.gradle.plugin.android-app"
			displayName = "Android App Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Android applications.
				
				Various utilities and setup for minimal configuration of apps.
				
				Features:
				 * Automatic singing based on conventional names.
				 * Automatic minification setup including common rules.
				 * Automatic versioning based on version.properties and VCS.
				 * Rename APK file name to contain more information.
				 * Release artifact generation (ZIP including APK and other files).
			""".trimIndent()
			tags.set(setOf("conventions", "android", "versioning", "proguard"))
			implementationClass = "net.twisterrob.gradle.android.AndroidAppPlugin"
		}
		create("androidLibrary") {
			id = "net.twisterrob.gradle.plugin.android-library"
			displayName = "Android Library Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Android libraries.
				
				Various utilities and setup for minimal configuration of libraries.
			""".trimIndent()
			tags.set(setOf("conventions", "android"))
			implementationClass = "net.twisterrob.gradle.android.AndroidLibraryPlugin"
		}
		create("androidTest") {
			id = "net.twisterrob.gradle.plugin.android-test"
			displayName = "Android Test Convention Plugin"
			description = """
				TWiStErRob's Convention plugin for Android test modules.
			""".trimIndent()
			tags.set(setOf("conventions", "android"))
			implementationClass = "net.twisterrob.gradle.android.AndroidTestPlugin"
		}
	}
}

dependencies {
	api(projects.plugin.base)
	api(projects.plugin.versioning)
	api(projects.plugin.signing)
	api(projects.plugin.languages)
	api(projects.plugin.release)
	api(projects.plugin.building)
	api(projects.plugin.reporting)
	api(projects.plugin.settings)
}

tasks.register("tests") {
	dependsOn(allprojects.map { it.tasks.named("test") })
}

allprojects {
	tasks.withType<Test>().configureEach {
		onlyIf {
			it.project.property("net.twisterrob.test.android.pluginVersion").toString() >= "4.0.0"
		}
	}
}
