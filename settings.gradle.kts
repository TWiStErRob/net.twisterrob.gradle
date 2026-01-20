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
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	//id("net.twisterrob.graph")
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
include(":compat:agp-81x")
include(":compat:agp-82x")
include(":compat:agp-83x")
include(":compat:agp-84x")
include(":compat:agp-85x")
include(":compat:agp-86x")
include(":compat:agp-87x")
include(":compat:agp-88x")
include(":compat:agp-89x")
include(":compat:agp-810x")
include(":compat:agp-811x")
include(":compat:agp-812x")
include(":compat:agp-813x")
include(":compat:agp-90x")
include(":compat:gradle")
include(":compat:kotlin-base")

include(":browser:pmd")
include(":browser:checkstyle")
include(":browser:lint")
include(":browser:agp-81x")
include(":browser:agp-82x")
include(":browser:agp-83x")
include(":browser:agp-84x")
include(":browser:agp-85x")
include(":browser:agp-86x")
include(":browser:agp-87x")
include(":browser:agp-88x")
include(":browser:agp-89x")
include(":browser:agp-810x")
include(":browser:agp-811x")
include(":browser:agp-812x")
include(":browser:agp-813x")
include(":browser:agp-90x")
include(":browser:kotlin-18x")
include(":browser:kotlin-19x")
include(":browser:kotlin-20x")
include(":browser:kotlin-21x")
include(":browser:kotlin-22x")

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

// TODEL Gradle 9.1 vs detekt 1.23.8 https://github.com/detekt/detekt/issues/8452
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"The ReportingExtension.file(String) method has been deprecated. " +
			"This is scheduled to be removed in Gradle 10. " +
			"Please use the getBaseDirectory().file(String) or getBaseDirectory().dir(String) method instead. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_9.html#reporting_extension_file",
	"at io.gitlab.arturbosch.detekt.DetektPlugin.apply(DetektPlugin.kt:28)",
)

// TODEL Gradle 9.1 vs IDEA 2025.2.1 https://youtrack.jetbrains.com/issue/IDEA-379286
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"The Configuration.isVisible method has been deprecated. " +
			"This is scheduled to be removed in Gradle 10. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_9.html#deprecate-visible-property",
	// Don't include line number, because of recent changes to the file.
	// It's line 89 in 2025.2.1, but in upcoming IDEA releases it will move twice.
	"at org.jetbrains.plugins.gradle.tooling.builder.ProjectExtensionsDataBuilderImpl\$Companion.collectConfigurations(ProjectExtensionsDataBuilderImpl.kt:",
)
