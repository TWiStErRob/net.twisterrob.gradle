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
	id("net.twisterrob.gradle.plugin.settings") version "0.18-SNAPSHOT"
	id("net.twisterrob.gradle.plugin.nagging") version "0.18-SNAPSHOT"
}

dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		google()
		mavenCentral()
	}
}

if (!JavaVersion.current().isJava11Compatible) {
	error("Java 11+ is required to build this project, found: ${JavaVersion.current()}.")
}

val gradleVersion: String = GradleVersion.current().version

// TODEL Gradle 8.2 vs AGP 8.0.2 https://issuetracker.google.com/issues/279306626
// Gradle 8.2 M1 added nagging for BuildIdentifier.*, which was not replaced in AGP 8.0.x yet.
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"The BuildIdentifier.isCurrentBuild() method has been deprecated. " +
			"This is scheduled to be removed in Gradle 9.0. " +
			"Use getBuildPath() to get a unique identifier for the build. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#build_identifier_name_and_current_deprecation",
	"at com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils.getBuildId(BuildMapping.kt:40)"
)
