plugins {
	id("org.gradle.java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm")
	id("org.gradle.java-test-fixtures")
	id("net.twisterrob.gradle.build.publishing")
	id("net.twisterrob.gradle.build.detekt")
}

base.archivesName.set("twister-convention-reporting")
description = "Reporting Convention Plugin: Gradle tasks to support reporting."

dependencies {
	implementation(gradleApiWithoutKotlin())
	api(projects.plugin.base)
	compileOnly(libs.android.gradle)
	// Need com.android.utils.FileUtils for TestReportGenerator.generate().
	compileOnly(libs.android.tools.common)

	// This plugin is part of the net.twisterrob.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
}

disableGradlePluginValidation()
