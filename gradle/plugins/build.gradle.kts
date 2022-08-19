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
	// TODEL hack from https://github.com/gradle/gradle/issues/15383#issuecomment-779893192 (there are more parts to this)
	implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
	implementation("com.gradle.enterprise:com.gradle.enterprise.gradle.plugin:${libs.versions.gradle.enterprise.get()}")
}
