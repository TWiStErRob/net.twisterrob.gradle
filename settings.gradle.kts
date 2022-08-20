import net.twisterrob.gradle.plugins.settings.TargetJvmVersionRule

// TODEL https://github.com/gradle/gradle/issues/18971
rootProject.name = "net-twisterrob-gradle"

pluginManagement {
	includeBuild("gradle/plugins")
}

plugins {
	id("com.gradle.enterprise")
	// Allows using classes / functions from gradle/plugins project.
	id("net.twisterrob.gradle.plugins.settings")
}

enableFeaturePreviewQuietly("TYPESAFE_PROJECT_ACCESSORS", "Type-safe project accessors")

include(":quality")
include(":common")
include(":test")
include(":test:internal")

listOf("checkstyle", "pmd").forEach {
	include(":${it}")
	project(":${it}").projectDir = file("checkers/${it}")
}

include(":compat:agp")
include(":compat:agp-base")
include(":compat:agp-40x")
include(":compat:agp-41x")
include(":compat:agp-42x")
include(":compat:agp-70x")
include(":compat:agp-71x")
include(":compat:agp-72x")
include(":compat:agp-73x")
include(":compat:agp-74x")
include(":compat:agp-latest")
include(":compat:gradle")

include(":plugin")
include(":plugin:base")
include(":plugin:versioning")
include(":plugin:languages")
include(":plugin:signing")
include(":plugin:release")
include(":plugin:building")
include(":plugin:reporting")

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
	rulesMode.set(RulesMode.PREFER_SETTINGS)
	components {
		withModule<TargetJvmVersionRule>("com.android.tools.build:gradle") { params(8)}
		withModule<TargetJvmVersionRule>("com.android.tools.build:gradle-api") { params(8)}
		withModule<TargetJvmVersionRule>("com.android.tools.build:gradle-settings-api") { params(8)}
		withModule<TargetJvmVersionRule>("com.android.tools.build:builder") { params(8)}
		withModule<TargetJvmVersionRule>("com.android.tools.build:builder-test-api") { params(8)}
		withModule<TargetJvmVersionRule>("com.android.tools.build:aaptcompiler") { params(8)}
		withModule<TargetJvmVersionRule>("com.android.tools.build:aapt2-proto") { params(8)}
		withModule<TargetJvmVersionRule>("com.android.tools.build:builder-model") { params(8)}
		withModule<TargetJvmVersionRule>("com.android.tools.build:manifest-merger") { params(8)}
	}
}
