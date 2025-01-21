import net.twisterrob.gradle.build.dependencies.addJarToClasspathOfPlugin

plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-convention-release"
description = "Release Convention Plugin: Gradle Plugin to handle conventional releasing."

gradlePlugin {
	disableGradlePluginValidation(project)
	plugins {
		// Not created, internal plugins only used by class reference.
	}
}

dependencies {
	implementation(gradleApi())
	api(projects.plugin.base)
	implementation(projects.plugin.versioning) // TODO decouple
	compileOnly(libs.android.gradle)
	// SdkConstants.FD_INTERMEDIATES
	compileOnly(libs.android.tools.common)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))

	// This plugin is part of the net.twisterrob.gradle.plugin.android-app plugin, not designed to work on its own.
	testInjectedPluginClasspath(projects.plugin)
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}

	testFixturesImplementation(projects.compat.agpBase)
}

// net.twisterrob.gradle.android.BuildDateKt.getBuiltDate needs the manifest.
// The manifest is generated centrally by root build.gradle.kts
addJarToClasspathOfPlugin()

tasks.named<Test>("test") {
	if (javaVersion.isJava9Compatible) {
		// Java 17 vs JUnit Pioneer https://junit-pioneer.org/docs/environment-variables/#warnings-for-reflective-access
		// NOTE: This didn't reproduce locally only on GHA CI.
		// Example test: net.twisterrob.gradle.android.AndroidReleasePluginIntgTest
		// org.junit.jupiter.api.extension.ExtensionConfigurationException:
		// Cannot access Java runtime internals to modify environment variables.
		// Have a look at the documentation for possible solutions:
		// https://junit-pioneer.org/docs/environment-variables/#warnings-for-reflective-access
		// Caused by: java.lang.reflect.InaccessibleObjectException:
		// Unable to make field private final java.util.Map java.util.Collections$UnmodifiableMap.m accessible:
		// module java.base does not "opens java.util" to unnamed module @f381794
		jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
		jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
	}
}
