// TODEL https://github.com/gradle/gradle/issues/18971
rootProject.name = "net-twisterrob-gradle"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

include(":quality")
include(":common")
include(":test")
include(":test:internal")

listOf("checkstyle", "pmd").forEach {
	include(":${it}")
	project(":${it}").projectDir = file("checkers/${it}")
}

include(":plugin")
include(":plugin:base")
include(":plugin:versioning")

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
