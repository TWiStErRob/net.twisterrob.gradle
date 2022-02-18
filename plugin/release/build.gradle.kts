plugins {
	kotlin
	id("java-gradle-plugin")
	`java-test-fixtures`
	id("net.twisterrob.gradle.build.publishing")
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
