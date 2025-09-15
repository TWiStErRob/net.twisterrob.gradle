import net.twisterrob.gradle.doNotNagAbout

rootProject.name = "graph"

// REPORT AGP spams because of missing Task.usesService.
//enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

plugins {
	id("net.twisterrob.gradle.plugin.nagging") version "0.18"
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
