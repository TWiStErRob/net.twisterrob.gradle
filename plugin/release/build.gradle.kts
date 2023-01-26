plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
	id("org.gradle.java-test-fixtures")
}

base.archivesName.set("twister-convention-release")
description = "Release Convention Plugin: Gradle Plugin to handle conventional releasing."

dependencies {
	implementation(gradleApiWithoutKotlin())
	api(projects.plugin.base)
	implementation(projects.plugin.versioning) // TODO decouple
	compileOnly(libs.android.gradle)
	// SdkConstants.FD_INTERMEDIATES
	compileOnly(libs.android.tools.common)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp)

	// This plugin is part of the net.twisterrob.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))

	testFixturesImplementation(projects.compat.agpBase)
}

disableGradlePluginValidation()

// net.twisterrob.gradle.android.BuildDateKt.getBuiltDate needs the manifest.
// The manifest is generated centrally by root build.gradle.kts
addJarToClasspathOfPlugin()

tasks.named<Test>("test") {
	// TODEL throws initializationError, probably because Java 8 is used for bytecode, but running on Java 19.
	// Example test: AndroidReleasePluginIntgTest (but only on CI!)
	jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
}
