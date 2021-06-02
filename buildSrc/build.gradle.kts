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

kotlinDslPluginOptions {
	// https://docs.gradle.org/4.10.3/userguide/kotlin_dsl.html#sec:kotlin_compiler_arguments
	experimentalWarning.set(false)
	// progressive.set(true)
}

repositories {
	mavenCentral()
	// Note on `plugins { }`: when the version is declared in the plugins block (`plugins { id(...) version "..." }`),
	// the referenced dependencies are visible by IntelliJ Gradle Sync, but the breakpoints are not hit.
	// Declaring all the dependencies in this project resolves this issue.
	gradlePluginPortal()
}

val kotlin_version: String by project
val nexus_version: String by project

dependencies {
	implementation(kotlin("gradle-plugin", version = kotlin_version))
	implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:${kotlin_version}"))
	implementation(kotlin("compiler-embeddable", version = kotlin_version))
	implementation("org.jetbrains.dokka:dokka-gradle-plugin:${kotlin_version}")
	implementation("io.github.gradle-nexus:publish-plugin:${nexus_version}")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	// TODO Using kotlin incremental compilation shows up regardless of verbose
	kotlinOptions.verbose = true
//	kotlinOptions.allWarningsAsErrors = true
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
