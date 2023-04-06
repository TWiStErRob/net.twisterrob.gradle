plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
}

gradlePlugin {
	// Necessary to apply java-gradle-plugin so that the dependent plugins get exposed to tests correctly.
	// No own plugin is published from this project.
	disableGradlePluginValidation()
}

dependencies {
	implementation(projects.plugin)
	implementation(projects.quality)
	implementation(projects.test)
	implementation(projects.compat.kotlinBase)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}
	testInjectedPluginClasspath(libs.kotlin.gradle) {
		version { require(property("net.twisterrob.test.kotlin.pluginVersion").toString()) }
	}
}
