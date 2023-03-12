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
				maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
					name = "Sonatype 01"
					mavenContent {
						snapshotsOnly()
					}
				}
			}
			filter {
				includeGroupByRegex("""^net\.twisterrob\.gradle\.plugin\..*$""")
				includeGroupByRegex("""^net\.twisterrob\.gradle$""")
			}
		}
	}
}

plugins {
	id("net.twisterrob.gradle.plugin.settings") version "0.16-SNAPSHOT"
}

if (!JavaVersion.current().isJava11Compatible) {
	error("Java 11+ is required to build this project, found: ${JavaVersion.current()}.")
}
