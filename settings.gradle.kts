import net.twisterrob.gradle.build.dsl.isCI
import net.twisterrob.gradle.build.settings.enableFeaturePreviewQuietly
import net.twisterrob.gradle.doNotNagAbout

//net.twisterrob.gradle.isDoNotNagAboutDiagnosticsEnabled = true

// TODEL https://github.com/gradle/gradle/issues/18971
rootProject.name = "net-twisterrob-gradle"

pluginManagement {
	includeBuild("gradle/plugins")
	includeBuild("graph")
	repositories {
		mavenCentral()
		gradlePluginPortal()
		google()
	}
}

plugins {
	// Allows using classes / functions from gradle/plugins project.
	id("net.twisterrob.gradle.build.settings")
	id("net.twisterrob.gradle.build.develocity")
	id("org.gradle.toolchains.foojay-resolver-convention") version("0.10.0")
}

// buildscript { enableDependencyLocking(settings) }

enableFeaturePreviewQuietly("TYPESAFE_PROJECT_ACCESSORS", "Type-safe project accessors")

include(":quality")
include(":common")
include(":test")
include(":test:integration")
include(":test:internal")
include(":test:internal:runtime")

listOf("checkstyle", "pmd").forEach {
	include(":${it}")
	project(":${it}").projectDir = file("checkers/${it}")
}

include(":compat:agp")
include(":compat:agp-base")
include(":compat:agp-70x")
include(":compat:agp-71x")
include(":compat:agp-72x")
include(":compat:agp-73x")
include(":compat:agp-74x")
include(":compat:agp-80x")
include(":compat:agp-81x")
include(":compat:agp-82x")
include(":compat:agp-83x")
include(":compat:agp-84x")
include(":compat:agp-85x")
include(":compat:agp-86x")
include(":compat:agp-87x")
include(":compat:agp-88x")
include(":compat:agp-89x")
include(":compat:gradle")
include(":compat:kotlin-base")

include(":browser:pmd")
include(":browser:checkstyle")
include(":browser:lint")
include(":browser:agp-40x")
include(":browser:agp-41x")
include(":browser:agp-42x")
include(":browser:agp-70x")
include(":browser:agp-71x")
include(":browser:agp-72x")
include(":browser:agp-73x")
include(":browser:agp-74x")
include(":browser:agp-80x")
include(":browser:agp-81x")
include(":browser:agp-82x")
include(":browser:agp-83x")
include(":browser:agp-84x")
include(":browser:agp-85x")
include(":browser:agp-86x")
include(":browser:agp-87x")
include(":browser:agp-88x")
include(":browser:agp-89x")
include(":browser:kotlin-14x")
include(":browser:kotlin-16x")
include(":browser:kotlin-19x")

include(":plugin")
include(":plugin:base")
include(":plugin:versioning")
include(":plugin:languages")
include(":plugin:signing")
include(":plugin:release")
include(":plugin:building")
include(":plugin:reporting")
include(":plugin:settings")

if (settings.extra["net.twisterrob.gradle.build.includeExamples"].toString().toBoolean()) {
	includeBuild("docs/examples/local")
	includeBuild("docs/examples/snapshot")
	includeBuild("docs/examples/release")
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		google()
		mavenCentral()
		// for Kotlin-DSL
		maven { setUrl("https://repo.gradle.org/gradle/libs-releases-local/") }
	}
	versionCatalogs {
		create(defaultLibrariesExtensionName.get()) {
			// Implicit behavior: from(files("gradle/libs.versions.toml"))
			// Load additional libraries from other files:
			load(file("gradle/agp.versions.toml"))
			load(file("gradle/kotlin.versions.toml"))
		}
	}
}


val gradleVersion: String = GradleVersion.current().version

// TODEL Gradle 8.2 sync in IDEA 2023.1 https://youtrack.jetbrains.com/issue/IDEA-320266.
@Suppress("detekt.MaxLineLength", "detekt.StringLiteralDuplication")
if ((System.getProperty("idea.version") ?: "") < "2023.3") {
	// There are ton of warnings, ignoring them all by their class names in one suppression.
	doNotNagAbout(
		Regex(
			"^" +
					"(" +
					Regex.escape("The Project.getConvention() method has been deprecated. ") +
					"|" +
					Regex.escape("The org.gradle.api.plugins.Convention type has been deprecated. ") +
					"|" +
					Regex.escape("The org.gradle.api.plugins.JavaPluginConvention type has been deprecated. ") +
					")" +
					Regex.escape("This is scheduled to be removed in Gradle 9.0. ") +
					Regex.escape("Consult the upgrading guide for further information: ") +
					Regex.escape("https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#") +
					".*" +
					"(" +
					Regex.escape("at org.jetbrains.kotlin.idea.gradleTooling.KotlinTasksPropertyUtilsKt.") +
					"|" +
					Regex.escape("at org.jetbrains.plugins.gradle.tooling.util.JavaPluginUtil.") +
					"|" +
					Regex.escape("at org.jetbrains.plugins.gradle.tooling.builder.ExternalProjectBuilderImpl.") +
					"|" +
					Regex.escape("at org.jetbrains.plugins.gradle.tooling.builder.ProjectExtensionsDataBuilderImpl.") +
					")" +
					".*$"
		)
	)
} else {
	val error: (String) -> Unit = if (isCI) ::error else logger::warn
	error("IDEA version changed, please review hack.")
}

// TODEL Gradle 8.2 sync in IDEA 2023.1 https://youtrack.jetbrains.com/issue/IDEA-320307.
@Suppress("detekt.MaxLineLength", "detekt.StringLiteralDuplication")
if ((System.getProperty("idea.version") ?: "") < "2023.3") {
	@Suppress("detekt.MaxLineLength", "detekt.StringLiteralDuplication")
	doNotNagAbout(
		"The BuildIdentifier.getName() method has been deprecated. " +
				"This is scheduled to be removed in Gradle 9.0. " +
				"Use getBuildPath() to get a unique identifier for the build. " +
				"Consult the upgrading guide for further information: " +
				"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#build_identifier_name_and_current_deprecation",
		// There are 4 stack traces coming to this line, ignore them all at once.
		"at org.jetbrains.plugins.gradle.tooling.util.resolve.DependencyResolverImpl.resolveDependencies(DependencyResolverImpl.java:266)"
	)
} else {
	val error: (String) -> Unit = if (isCI) ::error else logger::warn
	error("IDEA version changed, please review hack.")
}
