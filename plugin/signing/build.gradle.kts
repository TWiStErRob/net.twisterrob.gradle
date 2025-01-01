plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName = "twister-convention-signing"
description = "Signing Convention Plugin: Gradle Plugin to apply Android Signing conventions."

gradlePlugin {
	disableGradlePluginValidation(project)
	plugins {
		// Not created, internal plugins only used by class reference.
	}
}

dependencies {
	implementation(gradleApi())
	api(projects.plugin.base)
	compileOnly(libs.android.gradle)

	// This plugin is part of the net.twisterrob.gradle.plugin.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(projects.compat.agpBase)
	testImplementation(testFixtures(projects.plugin.base))
	testInjectedPluginClasspath(libs.android.gradle) {
		version { require(property("net.twisterrob.test.android.pluginVersion").toString()) }
	}
}

tasks.withType<Test>().configureEach {
	javaLauncher = javaToolchains.launcherFor {
		languageVersion = providers.gradleProperty("net.twisterrob.test.gradle.javaVersion")
			.map(JavaLanguageVersion::of)
	}
}
