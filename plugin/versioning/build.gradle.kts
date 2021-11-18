plugins {
	kotlin
	id("java-gradle-plugin")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-plugins-version")
description = "Versioning Convention Plugin: Gradle Plugin to set up versioning through properties and DSL."

gradlePlugin {
	plugins {
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())
	implementation(projects.common)
	implementation(projects.plugin.base)
	implementation(libs.svnkit)
	implementation(libs.svnkit.cli)
	implementation(libs.jgit)
	compileOnly(libs.android.gradle)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
}
