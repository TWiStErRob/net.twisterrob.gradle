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
}

plugins {
	id("net.twisterrob.gradle.plugin.settings") version "0.15.1"
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
	}
}

if (!JavaVersion.current().isJava11Compatible) {
	error("Java 11+ is required to build this project, found: ${JavaVersion.current()}.")
}
