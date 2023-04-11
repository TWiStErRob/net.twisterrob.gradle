import net.twisterrob.gradle.build.compilation.JavaCompatibilityPlugin
import net.twisterrob.gradle.build.detekt.DetektPlugin

project.plugins.apply("org.gradle.java-library")

project.plugins.apply("org.jetbrains.kotlin.jvm")
project.plugins.apply(JavaCompatibilityPlugin::class)

project.plugins.apply(DetektPlugin::class)
