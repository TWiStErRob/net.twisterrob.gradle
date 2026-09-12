package net.twisterrob.gradle.java

@Suppress("detekt.AbstractClassCanBeConcreteClass") // Gradle convention.
abstract class JavaLibPlugin : BaseJavaPlugin() {

	override fun applyDefaultPlugin() {
		project.plugins.apply("java-library")
	}
}
