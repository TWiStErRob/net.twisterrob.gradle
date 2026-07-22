import net.twisterrob.gradle.doNotNagAbout

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
				mavenLocal()
			}
			filter {
				includeGroupByRegex("""^net\.twisterrob\.gradle\.plugin\.[^.]+$""")
				includeGroupByRegex("""^net\.twisterrob\.gradle$""")
			}
		}
	}
}

plugins {
	id("net.twisterrob.gradle.plugin.settings") version "0.20-SNAPSHOT"
	id("net.twisterrob.gradle.plugin.nagging") version "0.20-SNAPSHOT"
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

// TODEL Gradle 9.6 vs AGP 9.2, fixed in AGP 9.3 https://issuetracker.google.com/issues/495889752
doNotNagAbout(
	"Using a Project object as a dependency notation has been deprecated. " +
			"This will fail with an error in Gradle 10. " +
			"Please use the project(String) method on DependencyHandler or the createProjectDependency(String) method on DependencyFactory instead.",
	"at com.android.build.gradle.internal.dependency.VariantDependenciesBuilder.build(VariantDependenciesBuilder.java:", // :279, :333
)
