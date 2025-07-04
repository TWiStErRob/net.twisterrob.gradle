pluginManagement {
	repositories {
		mavenCentral()
		//gradlePluginPortal() // not used
		exclusiveContent {
			forRepository {
				maven("https://ossrh-staging-api.central.sonatype.com/service/local/repositories/net.twisterrob--a6fd9564-39a2-402f-ab53-147cd57ff0b3/content/") {
					name = "Sonatype Staging for net.twisterrob"
				}
			}
			filter {
				includeVersionByRegex("""^net\.twisterrob\.gradle$""", ".*", "^${Regex.escape("0.0.1")}$")
				includeVersionByRegex("""^net\.twisterrob\.gradle\.plugin\.[^.]+$""", ".*", "^${Regex.escape("0.0.1")}$")
			}
		}
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
	id("net.twisterrob.gradle.plugin.settings") version "0.0.1"
}

dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		exclusiveContent {
			forRepository {
				maven("https://ossrh-staging-api.central.sonatype.com/service/local/repositories/net.twisterrob--a6fd9564-39a2-402f-ab53-147cd57ff0b3/content/") {
					name = "Sonatype Staging for net.twisterrob"
				}
			}
			filter {
				includeVersionByRegex("""^net\.twisterrob\.gradle$""", ".*", "^${Regex.escape("0.0.1")}$")
				includeVersionByRegex("""^net\.twisterrob\.gradle\.plugin\.[^.]+$""", ".*", "^${Regex.escape("0.0.1")}$")
			}
		}
		google()
		mavenCentral()
	}
}

if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
	error("Java 17+ is required to build this project, found: ${JavaVersion.current()}.")
}
