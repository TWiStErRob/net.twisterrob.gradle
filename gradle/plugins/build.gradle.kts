plugins {
	`kotlin-dsl`
	//alias(libs.plugins.kotlin) // Can't apply since there's a mismatch between embedded Kotlin and latest Kotlin.
	id("java-gradle-plugin")
}

gradlePlugin {
	plugins {
		create("net.twisterrob.gradle.plugins.settings") {
			id = "net.twisterrob.gradle.plugins.settings"
			implementationClass = "net.twisterrob.gradle.plugins.settings.SettingsPlugin"
		}

		// Re-exposure of plugin from dependency. Gradle doesn't expose the plugin itself, even with api().
		create("com.gradle.enterprise") {
			id = "com.gradle.enterprise"
			implementationClass = "com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin"
			dependencies {
				implementation(libs.gradle.enterprise)
			}
		}

		create("publishing") {
			id = "net.twisterrob.gradle.build.publishing"
			implementationClass = "net.twisterrob.gradle.build.PublishingPlugin"
		}
	}
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

// Note on `plugins { }`: when the version is declared in the plugins block (`plugins { id(...) version "..." }`),
// the referenced dependencies are visible by IntelliJ Gradle Sync, but the breakpoints are not hit.
// Declaring all the dependencies in this project resolves this issue.
dependencies {
	implementation(libs.kotlin.gradle)
	implementation(libs.kotlin.dokka)
	compileOnly(libs.nexus)

	// TODEL hack from https://github.com/gradle/gradle/issues/15383#issuecomment-779893192 (there are more parts to this)
	compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions.verbose = true
	kotlinOptions.allWarningsAsErrors = true
}
