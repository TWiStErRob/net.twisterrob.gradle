pluginManagement {
	repositories {
		mavenCentral()
		//gradlePluginPortal() // not used
		exclusiveContent {
			forRepository {
				google()
			}
			filter {
				includeGroupByRegex("""^com\.android(\..*)?$""")
				includeGroupByRegex("""^com\.google\.android\..*$""")
				includeGroupByRegex("""^com\.google\.testing\.platform$""")
				includeGroupByRegex("""^androidx\..*$""")
			}
		}
	}
	resolutionStrategy {
		eachPlugin {
			when (requested.id.id) {
				"com.android.application" ->
					useModule("com.android.tools.build:gradle:${requested.version}")
				"net.twisterrob.quality" ->
					useModule("net.twisterrob.gradle:twister-quality:${requested.version}")
				"net.twisterrob.settings" ->
					useModule("net.twisterrob.gradle:twister-convention-settings:${requested.version}")
			}
		}
	}
}

plugins {
	id("net.twisterrob.settings") version "0.15.1"
}

if (!JavaVersion.current().isJava11Compatible) {
	error("Java 11+ is required to build this project, found: ${JavaVersion.current()}.")
}
