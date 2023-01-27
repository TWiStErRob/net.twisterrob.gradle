plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-convention-settings")
description = "Settings Convention Plugin: Gradle Plugin to apply in settings.gradle files."

gradlePlugin {
	plugins {
		create("net.twisterrob.settings") {
			id = "net.twisterrob.settings"
			implementationClass = "net.twisterrob.gradle.settings.SettingsPlugin"
		}
	}
}

kotlin {
	sourceSets.named("main").configure {
		// Split up the sources into multiple folders, so gradle/plugins can re-use some of them.
		// Creating an extra folder for only publicly used sources.
		// Therefore, everything in src/main/kotlin is re-used between this project and gradle/plugins.
		// See gradle/plugins/build.gradle.kts > kotlin.sourceSets for more info.
		kotlin.srcDir("src/main/kotlin-published")
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
}

if (project.property("net.twisterrob.gradle.build.hackKotlinMetadata") == "true") {
	tasks.jar {
		exclude("META-INF/twister-convention-settings.kotlin_module")
	}
}
