import net.twisterrob.gradle.doNotNagAbout

rootProject.name = "graph"

// REPORT AGP spams because of missing Task.usesService.
//enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

plugins {
	id("net.twisterrob.gradle.plugin.nagging") version "0.17"
}

val gradleVersion: String = GradleVersion.current().version

// TODEL Kover >0.9.0 https://github.com/Kotlin/kotlinx-kover/issues/716
@Suppress("detekt.MaxLineLength", "detekt.StringLiteralDuplication")
doNotNagAbout(
	"Calling configuration method 'attributes(Action)' is deprecated for configuration 'kover', which has permitted usage(s):\n" +
			"\tDeclarable - this configuration can have dependencies added to it\n" +
			"This method is only meant to be called on configurations which allow the (non-deprecated) usage(s): 'Consumable, Resolvable'. " +
			"This behavior has been deprecated. " +
			"This behavior is scheduled to be removed in Gradle 9.0. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_configuration_usage",
	"at kotlinx.kover.gradle.plugin.appliers.PrepareKoverKt\$prepare\$koverBucketConfiguration\$1.execute(PrepareKover.kt:26)",
)
