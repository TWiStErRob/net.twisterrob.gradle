package net.twisterrob.gradle.build.compilation

import net.twisterrob.gradle.build.dsl.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withModule

class JavaCompatibilityPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.dependencies.components {
			// AGP 8.0.0-alpha10 requires Java 17 (https://issuetracker.google.com/issues/241546506)
			// But Google didn't change the Java compiler and class file format to be Java 17 yet.
			// Even AGP 8.1.0-beta03 is published with Java 11 bytecode
			// according to its gradle-module-metadata and class header.
			// AGP 8.2.0-alpha03 onwards is published with Java 17 bytecode according to its metadata.

			// ```
			// Execution failed for task ':common:compileKotlin'.
			// > Could not resolve all files for configuration ':common:compileClasspath'.
			// > Could not resolve com.android.tools.build:gradle:8.2.0-alpha05.
			// Required by: project :common
			// > No matching variant of com.android.tools.build:gradle:8.2.0-alpha05 was found.
			// The consumer was configured to find a library for use during compile-time, compatible with Java 11,
			// preferably not packaged as a jar, preferably optimized for standard JVMs,
			// and its dependencies declared externally,
			// as well as attribute 'org.jetbrains.kotlin.platform.type' with value 'jvm' but:
			// - Variant 'apiElements' capability com.android.tools.build:gradle:8.2.0-alpha05 declares ...:
			// - Incompatible because this component declares a component, compatible with Java 17
			// and the consumer needed a component, compatible with Java 11
			// ```
			// This project still uses Java 11 for compatibility with AGP 7.x.
			// Let's rewrite the metadata of AGP 8.2, so that the produced jars can still be used with Java 11.
			// https://docs.gradle.org/current/userguide/component_metadata_rules.html

			val javaVersion = target.libs.versions.java.get()
				.let { JavaVersion.toVersion(it).majorVersion.toInt() }

			//@formatter:off
			withModule<LatestAgpTargetJvmLoweringRule>("com.android.tools.build:gradle") { params(javaVersion) }
			withModule<LatestAgpTargetJvmLoweringRule>("com.android.tools.build:gradle-api") { params(javaVersion) }
			withModule<LatestAgpTargetJvmLoweringRule>("com.android.tools.build:gradle-settings-api") { params(javaVersion) }
			withModule<LatestAgpTargetJvmLoweringRule>("com.android.tools.build:builder") { params(javaVersion) }
			withModule<LatestAgpTargetJvmLoweringRule>("com.android.tools.build:builder-model") { params(javaVersion) }
			withModule<LatestAgpTargetJvmLoweringRule>("com.android.tools.build:builder-test-api") { params(javaVersion) }
			withModule<LatestAgpTargetJvmLoweringRule>("com.android.tools.build:manifest-merger") { params(javaVersion) }
			withModule<LatestAgpTargetJvmLoweringRule>("com.android.tools.build:aapt2-proto") { params(javaVersion) }
			withModule<LatestAgpTargetJvmLoweringRule>("com.android.tools.build:aaptcompiler") { params(javaVersion) }
			//@formatter:on
		}
	}
}
