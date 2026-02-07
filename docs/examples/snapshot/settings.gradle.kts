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
		exclusiveContent {
			forRepository {
				maven("https://central.sonatype.com/repository/maven-snapshots/") {
					name = "Central SNAPSHOTs"
					mavenContent {
						snapshotsOnly()
					}
				}
			}
			filter {
				includeGroupByRegex("""^net\.twisterrob\.gradle\.plugin\.[^.]+$""")
				includeGroupByRegex("""^net\.twisterrob\.gradle$""")
			}
		}
	}
}

plugins {
	id("net.twisterrob.gradle.plugin.settings") version "0.19-SNAPSHOT"
}

dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		google()
		mavenCentral()
	}
}

if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
	error("Java 17+ is required to build this project, found: ${JavaVersion.current()}.")
}
