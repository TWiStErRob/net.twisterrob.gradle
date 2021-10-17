import org.gradle.util.GradleVersion

rootProject.name = "net.twisterrob.gradle"

include(":quality")
include(":plugin")
include(":common")
include(":test")
include(":test:internal")

listOf("checkstyle", "pmd").forEach {
	include(":${it}")
	project(":${it}").projectDir = file("checkers/${it}")
}

// As part of making the publishing plugins stable,
// the 'deferred configurable' behavior of the 'publishing {}' block is now deprecated.
// https://docs.gradle.org/4.8/userguide/publishing_maven.html#publishing_maven:deferred_configuration.
if (GradleVersion.current().baseVersion < GradleVersion.version("5.0")) {
	enableFeaturePreview("STABLE_PUBLISHING")
}

if (settings.extra["net.twisterrob.gradle.build.includeExamples"].toString().toBoolean()) {
	includeBuild("docs/examples/local")
	includeBuild("docs/examples/snapshot")
	includeBuild("docs/examples/release")
}
