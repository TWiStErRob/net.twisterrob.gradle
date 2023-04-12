import net.twisterrob.gradle.build.dependencies.gradleApiWithoutKotlin
import net.twisterrob.gradle.build.dependencies.addJarToClasspathOfPlugin

plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
	id("org.gradle.java-test-fixtures")
}

base.archivesName.set("twister-convention-release")
description = "Release Convention Plugin: Gradle Plugin to handle conventional releasing."

gradlePlugin {
	disableGradlePluginValidation(project)
	plugins {
		// Not created, internal plugins only used by class reference.
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	api(projects.plugin.base)
	implementation(projects.plugin.versioning) // TODO decouple
	compileOnly(libs.android.gradle)
	// SdkConstants.FD_INTERMEDIATES
	compileOnly(libs.android.tools.common)
	implementation(projects.compat.agpBase)
	implementation(projects.compat.agp)

	// This plugin is part of the net.twisterrob.gradle.plugin.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}

	testFixturesImplementation(projects.compat.agpBase)
}

// net.twisterrob.gradle.android.BuildDateKt.getBuiltDate needs the manifest.
// The manifest is generated centrally by root build.gradle.kts
addJarToClasspathOfPlugin()
