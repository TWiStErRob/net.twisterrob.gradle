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
	gradlePluginPortal()
}

dependencies {
	implementation("com.gradle.enterprise:com.gradle.enterprise.gradle.plugin:${libs.versions.gradle.enterprise.get()}")
}
