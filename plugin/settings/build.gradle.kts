plugins {
	kotlin
	id("java-gradle-plugin")
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-convention-settings")
description = "Settings Convention Plugin: Gradle Plugin to apply in settings.gradle files."

gradlePlugin {
	plugins {
		create("net.twisterrob.settings") {
			id = "net.twisterrob.settings"
			implementationClass = "net.twisterrob.gradle.settings.SettingsPlugin"
		}
	}
}

dependencies {
	implementation(gradleApiWithoutKotlin())

	testImplementation(projects.test.internal)
}
