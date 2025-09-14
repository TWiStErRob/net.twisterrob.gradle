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
	id("net.twisterrob.gradle.plugin.settings") version "0.19-SNAPSHOT"
	id("net.twisterrob.gradle.plugin.nagging") version "0.19-SNAPSHOT"
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

val gradleVersion: String = GradleVersion.current().version

// TODEL Gradle 9.1 vs AGP 8.13 https://issuetracker.google.com/issues/444260628
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	Regex(
		"Declaring dependencies using multi-string notation has been deprecated. ".escape() +
				"This will fail with an error in Gradle 10. ".escape() +
				"Please use single-string notation instead: ".escape() +
				"\"${"com.android.tools.build:aapt2:".escape()}\\d+\\.\\d+\\.\\d+(-(alpha|beta|rc)\\d+)?-\\d+:(windows|linux|osx)${"\". ".escape()}" +
				"Consult the upgrading guide for further information: ".escape() +
				"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_9.html#dependency_multi_string_notation".escape() +
				".*",
	),
	//"at com.android.build.gradle.internal.res.Aapt2FromMaven\$Companion.create(Aapt2FromMaven.kt:139)",
)

// TODEL Gradle 9.1 vs AGP 8.13 https://issuetracker.google.com/issues/444260628
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	Regex(
		"Declaring dependencies using multi-string notation has been deprecated. ".escape() +
				"This will fail with an error in Gradle 10. ".escape() +
				"Please use single-string notation instead: ".escape() +
				"\"${"com.android.tools.lint:lint-gradle:".escape()}\\d+\\.\\d+\\.\\d+(-(alpha|beta|rc)\\d+)?${"\". ".escape()}" +
				"Consult the upgrading guide for further information: ".escape() +
				"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_9.html#dependency_multi_string_notation".escape() +
				".*",
	),
	//"at com.android.build.gradle.internal.lint.LintFromMaven\$Companion.from(AndroidLintInputs.kt:2850)",
)

private fun String.escape(): String = Regex.escape(this)
