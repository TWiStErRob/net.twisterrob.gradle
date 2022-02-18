@file:Suppress("PropertyName")

buildscript {
	repositories {
		mavenCentral()
	}

	dependencies {
		classpath(kotlin("gradle-plugin"))
	}
}

plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
}

repositories {
	mavenCentral()
	// Note on `plugins { }`: when the version is declared in the plugins block (`plugins { id(...) version "..." }`),
	// the referenced dependencies are visible by IntelliJ Gradle Sync, but the breakpoints are not hit.
	// Declaring all the dependencies in this project resolves this issue.
	gradlePluginPortal()
}

dependencies {
	implementation(projectLibs.kotlin.gradle)
	implementation(enforcedPlatform(projectLibs.kotlin.bom))
	implementation(projectLibs.kotlin.compiler)
	implementation(projectLibs.kotlin.dokka)
	compileOnly(projectLibs.nexus)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	// Do not use, no effect; will be overridden by kotlinDslPluginOptions.jvmTarget, see KotlinDslCompilerPlugins.
	//kotlinOptions.jvmTarget = *
	kotlinOptions.verbose = true
	kotlinOptions.allWarningsAsErrors = true
	kotlinOptions.freeCompilerArgs += listOf(
		// w: Some JAR files in the classpath have the Kotlin Runtime library bundled into them.
		// This may cause difficult to debug problems if there's a different version of the Kotlin Runtime library in the classpath.
		// w: ...\org.jetbrains.kotlin\kotlin-compiler-embeddable\1.1.4-3\...\kotlin-compiler-embeddable-1.1.4-3.jar:
		// Library has Kotlin runtime bundled into it
		"-Xskip-runtime-version-check"
	)
}

gradlePlugin {
	plugins {
		create("publishing") {
			id = "net.twisterrob.gradle.build.publishing"
			implementationClass = "net.twisterrob.gradle.build.PublishingPlugin"
		}
	}
}
