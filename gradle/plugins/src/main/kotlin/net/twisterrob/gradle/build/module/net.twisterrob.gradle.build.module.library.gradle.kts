import net.twisterrob.gradle.build.compilation.JavaCompatibilityPlugin
import net.twisterrob.gradle.build.detekt.DetektPlugin

plugins {
	id("org.gradle.java-library")
	id("org.jetbrains.kotlin.jvm")
}
plugins.apply(JavaCompatibilityPlugin::class)
plugins.apply(DetektPlugin::class)
