plugins {
	kotlin
	id("java-gradle-plugin")
	id("java-test-fixtures")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-versioning")
description = "Versioning Convention Plugin: Gradle Plugin to set up versioning through properties and DSL."

gradlePlugin {
	plugins {
		create("net.twisterrob.vcs") {
			id = "net.twisterrob.vcs"
			implementationClass = "net.twisterrob.gradle.vcs.VCSPlugin"
		}
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	implementation(projects.plugin.base)
	implementation(projects.compat.gradle)
	implementation(projects.compat.agpBase)
	implementation(libs.svnkit)
	implementation(libs.svnkit.cli)
	implementation(libs.jgit)
	compileOnly(libs.android.gradle)

	// This plugin is part of the net.twisterrob.android-app plugin, not designed to work on its own.
	runtimeOnly(projects.plugin)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))

	testFixturesApi(libs.svnkit)
	testFixturesApi(libs.jgit)
}
