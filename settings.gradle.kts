import net.twisterrob.gradle.build.dsl.isCI
import net.twisterrob.gradle.build.settings.enableFeaturePreviewQuietly
import net.twisterrob.gradle.doNotNagAbout

// TODEL https://github.com/gradle/gradle/issues/18971
rootProject.name = "net-twisterrob-gradle"

pluginManagement {
	includeBuild("gradle/plugins")
}

plugins {
	// Allows using classes / functions from gradle/plugins project.
	id("net.twisterrob.gradle.build.settings")
	id("net.twisterrob.gradle.build.enterprise")
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
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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
		}
	}
}


val gradleVersion: String = GradleVersion.current().version
