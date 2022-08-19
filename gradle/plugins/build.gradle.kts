plugins {
	`kotlin-dsl`
	id("java-gradle-plugin")
}

gradlePlugin {
	plugins {
		create("net.twisterrob.gradle.plugins.settings") {
			id = "net.twisterrob.gradle.plugins.settings"
			implementationClass = "net.twisterrob.gradle.plugins.settings.SettingsPlugin"
		}
	}
}

repositories {
	mavenCentral()
}
