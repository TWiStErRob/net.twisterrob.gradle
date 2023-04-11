import net.twisterrob.gradle.build.compilation.JavaCompatibilityPlugin
import net.twisterrob.gradle.build.detekt.DetektPlugin

plugins {
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.testing.runtime")
	id("net.twisterrob.gradle.build.testing.substitutions")
	id("net.twisterrob.gradle.build.compilation")
}
plugins.apply(JavaCompatibilityPlugin::class)
plugins.apply(DetektPlugin::class)
