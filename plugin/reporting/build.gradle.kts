plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-convention-reporting"
description = "Reporting Convention Plugin: Gradle tasks to support reporting."

gradlePlugin {
	disableGradlePluginValidation(project)
	plugins {
		// Not created, internal plugins only used by class reference.
	}
}

dependencies {
	implementation(gradleApi())
	api(projects.plugin.base)
	implementation(projects.compat.agp)
	compileOnly(libs.android.gradle)
	// Need com.android.utils.FileUtils for TestReportGenerator.generate().
	compileOnly(libs.android.tools.common)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))

	// This plugin is part of the net.twisterrob.gradle.plugin.android-app plugin, not designed to work on its own.
	testInjectedPluginClasspath(projects.plugin)
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}
}
