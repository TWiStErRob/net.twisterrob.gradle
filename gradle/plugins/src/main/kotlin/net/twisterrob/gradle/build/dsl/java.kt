package net.twisterrob.gradle.build.dsl

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getByName

/**
 * @see <a href="file://.../gradle-kotlin-dsl-accessors/.../src/org/gradle/kotlin/dsl/accessors.kt">Generated code</a>
 */
val Project.java: JavaPluginExtension
	get() = this.extensions.getByName<JavaPluginExtension>("java")

