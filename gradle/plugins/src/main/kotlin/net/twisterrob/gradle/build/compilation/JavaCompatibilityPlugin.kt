package net.twisterrob.gradle.build.compilation

import libs
import net.twisterrob.gradle.plugins.settings.TargetJvmVersionRule
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withModule

class JavaCompatibilityPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.dependencies.components {
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

			val javaVersion = target.libs.versions.java.get()
				.let { JavaVersion.toVersion(it).majorVersion.toInt() }

			withModule<TargetJvmVersionRule>("com.android.tools.build:gradle") { params(javaVersion) }
			withModule<TargetJvmVersionRule>("com.android.tools.build:gradle-api") { params(javaVersion) }
			withModule<TargetJvmVersionRule>("com.android.tools.build:gradle-settings-api") { params(javaVersion) }
			withModule<TargetJvmVersionRule>("com.android.tools.build:builder") { params(javaVersion) }
			withModule<TargetJvmVersionRule>("com.android.tools.build:builder-model") { params(javaVersion) }
			withModule<TargetJvmVersionRule>("com.android.tools.build:builder-test-api") { params(javaVersion) }
			withModule<TargetJvmVersionRule>("com.android.tools.build:manifest-merger") { params(javaVersion) }
			withModule<TargetJvmVersionRule>("com.android.tools.build:aapt2-proto") { params(javaVersion) }
			withModule<TargetJvmVersionRule>("com.android.tools.build:aaptcompiler") { params(javaVersion) }
		}
	}
}
