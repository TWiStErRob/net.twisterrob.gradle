// TODEL https://github.com/gradle/gradle/issues/18971
rootProject.name = "net-twisterrob-gradle"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

include(":quality")
include(":plugin")
include(":common")
include(":test")
include(":test:internal")

listOf("checkstyle", "pmd").forEach {
	include(":${it}")
	project(":${it}").projectDir = file("checkers/${it}")
}

if (settings.extra["net.twisterrob.gradle.build.includeExamples"].toString().toBoolean()) {
	includeBuild("docs/examples/local")
	includeBuild("docs/examples/snapshot")
	includeBuild("docs/examples/release")
}
