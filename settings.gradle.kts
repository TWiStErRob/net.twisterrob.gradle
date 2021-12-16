// TODEL https://github.com/gradle/gradle/issues/18971
rootProject.name = "net-twisterrob-gradle"

enableFeaturePreviewQuietly("TYPESAFE_PROJECT_ACCESSORS", "Type-safe project accessors")
enableFeaturePreviewQuietly("VERSION_CATALOGS", "Type-safe dependency accessors")

include(":quality")
include(":common")
include(":test")
include(":test:internal")

listOf("checkstyle", "pmd").forEach {
	include(":${it}")
	project(":${it}").projectDir = file("checkers/${it}")
}

include(":compat:agp")
include(":compat:agp-40x")
include(":compat:agp-41x")
include(":compat:agp-42x")
include(":compat:agp-70x")
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

plugins {
	id("com.gradle.enterprise") version "3.7.1"
}

gradleEnterprise {
	buildScan {
		termsOfServiceUrl = "https://gradle.com/terms-of-service"
		termsOfServiceAgree = "yes"
	}
}

/**
 * @see <a href="https://github.com/gradle/gradle/issues/19069">Feature request</a>
 */
fun Settings.enableFeaturePreviewQuietly(name: String, summary: String) {
	enableFeaturePreview(name)
	val logger: Any = org.gradle.util.internal.IncubationLogger::class.java
		.getDeclaredField("INCUBATING_FEATURE_HANDLER")
		.apply { isAccessible = true }
		.get(null)

	@Suppress("UNCHECKED_CAST")
	val features: MutableSet<String> = org.gradle.internal.featurelifecycle.LoggingIncubatingFeatureHandler::class.java
		.getDeclaredField("features")
		.apply { isAccessible = true }
		.get(logger) as MutableSet<String>

	features.add(summary)
}
