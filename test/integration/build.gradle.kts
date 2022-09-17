plugins {
	id("org.gradle.java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm")
}

dependencies {
	implementation(projects.plugin)
	implementation(projects.quality)
	implementation(projects.test)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
}

// Necessary to apply java-gradle-plugin so that the dependent plugins get exposed to tests correctly.
// No own plugin is published from this project.
disableGradlePluginValidation()

tasks.withType<Test>().configureEach {
	onlyIf {
		it.project.property("net.twisterrob.test.android.pluginVersion").toString() == "7.3.0" &&
		it.project.property("net.twisterrob.test.kotlin.pluginVersion").toString() == "1.6.21"
	}
}
