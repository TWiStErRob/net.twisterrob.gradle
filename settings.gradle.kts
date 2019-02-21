rootProject.name = "net.twisterrob.gradle"

include(":quality")
include(":common")
include(":test")
include(":test:internal")

listOf("checkstyle", "pmd").forEach {
	include(":$it")
	project(":$it").projectDir = file("checkers/$it")
}

// As part of making the publishing plugins stable,
// the 'deferred configurable' behavior of the 'publishing {}' block is now deprecated.
// https://docs.gradle.org/4.8/userguide/publishing_maven.html#publishing_maven:deferred_configuration.
enableFeaturePreview("STABLE_PUBLISHING")
