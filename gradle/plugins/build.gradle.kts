plugins {
	id("org.gradle.java-gradle-plugin")
	//alias(libs.plugins.kotlin) // Can't apply since there's a mismatch between embedded Kotlin and latest Kotlin.
	`kotlin-dsl`
	id("io.gitlab.arturbosch.detekt") version "1.21.0"
}

gradlePlugin {
	plugins {
		create("settings") {
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

		create("detekt") {
			id = "net.twisterrob.gradle.build.detekt"
			implementationClass = "net.twisterrob.gradle.build.DetektPlugin"
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
	implementation(libs.detekt)
	compileOnly(libs.nexus)

	// TODEL hack from https://github.com/gradle/gradle/issues/15383#issuecomment-779893192 (there are more parts to this)
	compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions.verbose = true
	kotlinOptions.allWarningsAsErrors = true
}

detekt {
	// TODEL https://github.com/detekt/detekt/issues/4926
	buildUponDefaultConfig = false
	allRules = true
	//debug = true
	config = project.rootProject.files("../../config/detekt/detekt.yml")
	baseline = project.rootProject.file("../../config/detekt/detekt-baseline-gradle-plugins.xml")
	basePath = project.rootProject.projectDir.resolve("../..").absolutePath

	parallel = true

	project.tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
		reports {
			html.required.set(true) // human
			txt.required.set(true) // console
		}
	}
}
