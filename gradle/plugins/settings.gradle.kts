import net.twisterrob.gradle.doNotNagAbout
import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser

dependencyResolutionManagement {
	repositories {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		mavenCentral()
		gradlePluginPortal()
	}
	versionCatalogs {
		create(defaultLibrariesExtensionName.get()) {
			from(files("../../gradle/libs.versions.toml"))
			// Note: it is necessary to load both libs and agp the same way as it's done
			// in rootProject.settings.gradle.kts, because otherwise there are unresolved references.
			TomlCatalogFileParser.parse(file("../../gradle/agp.versions.toml").toPath(), this)
		}
	}
}

plugins {
	id("net.twisterrob.gradle.plugin.settings") version "0.15.1"
}

val gradleVersion: String = GradleVersion.current().version

// TODEL Gradle 8.2 milestone 1 vs Kotlin 1.8.20 https://github.com/gradle/gradle/pull/24271#issuecomment-1546706115.
if (gradleVersion == "8.2-milestone-1") {
	@Suppress("MaxLineLength", "StringLiteralDuplication")
	(doNotNagAbout(
		Regex(
			Regex.escape("The resolvable usage is already allowed on configuration ") +
					"':.*?:testFixturesRuntimeClasspath'. " +
					Regex.escape("This behavior has been deprecated. ") +
					Regex.escape("This behavior is scheduled to be removed in Gradle 9.0. ") +
					Regex.escape("Remove the call to setCanBeResolved(true), it has no effect. ") +
					Regex.escape("Consult the upgrading guide for further information: ") +
					Regex.escape("https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#redundant_configuration_usage_activation") +
					".*" +
					// Task :generatePrecompiledScriptPluginAccessors
					Regex.escape("at org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory.KotlinCompilationDependencyConfigurationsFactoriesKt.KotlinCompilationDependencyConfigurationsContainer") +
					".*"
		)
	))
} else {
	error("Gradle version changed, please remove hack.")
}
