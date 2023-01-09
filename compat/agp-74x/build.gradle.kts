import net.twisterrob.gradle.plugins.settings.TargetJvmVersionRule

plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-agp-7.4.x")
description = "AGP Compatibility 7.4.x: Compatibility layer for Android Gradle Plugin 7.4.x."

dependencies {
	implementation(gradleApiWithoutKotlin())
	compileOnly(libs.android.gradle.v74x)
	components {
		// Between AGP 7.4.0-alpha04-09, Google changed the Java compiler and class file format to be Java 11. 
		// ```
		// Execution failed for task ':compat:agp-74x:compileKotlin'.
		// > Could not resolve all files for configuration ':compat:agp-74x:compileClasspath'.
		// > Could not resolve com.android.tools.build:gradle:7.4.0-alpha09.
		// Required by: project :compat:agp-74x
		// > No matching variant of com.android.tools.build:gradle:7.4.0-alpha09 was found.
		// The consumer was configured to find an API of a library compatible with Java 8,
		// preferably in the form of class files, preferably optimized for standard JVMs,
		// and its dependencies declared externally,
		// as well as attribute 'org.jetbrains.kotlin.platform.type' with value 'jvm' but:
		// - Variant 'apiElements' capability com.android.tools.build:gradle:7.4.0-alpha09 declares ...:
		// - Incompatible because this component declares a component compatible with Java 11
		// and the consumer needed a component compatible with Java 8
		// ```
		// This project still uses Java 8, so let's rewrite the metadata,
		// so that the produced jars can still be used with Java 8.
		// https://docs.gradle.org/current/userguide/component_metadata_rules.html
		withModule<TargetJvmVersionRule>("com.android.tools.build:gradle") { params(8) }
		withModule<TargetJvmVersionRule>("com.android.tools.build:gradle-api") { params(8) }
		withModule<TargetJvmVersionRule>("com.android.tools.build:builder") { params(8) }
		withModule<TargetJvmVersionRule>("com.android.tools.build:builder-model") { params(8) }
		withModule<TargetJvmVersionRule>("com.android.tools.build:manifest-merger") { params(8) }
	}
}
