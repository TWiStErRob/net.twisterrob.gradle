import org.gradle.util.GradleVersion

buildscript {
	repositories {
		gradlePluginPortal()
	}
	dependencies {
		// Using old syntax, because it's not possible to otherwise conditionally apply the plugin.
		classpath("org.gradle.toolchains:foojay-resolver:0.5.0")
	}
}

if (GradleVersion.version("8.0") <= GradleVersion.current().baseVersion) {
	// In Gradle 8.0 it became mandatory to add this a toolchain repository.
	// https://docs.gradle.org/current/userguide/upgrading_version_7.html#using_automatic_toolchain_downloading_without_having_a_repository_configured
	apply(plugin = "org.gradle.toolchains.foojay-resolver-convention")
}
