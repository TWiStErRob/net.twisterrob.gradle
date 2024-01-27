package net.twisterrob.gradle.java

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class JavaLibPlugin : BaseJavaPlugin() {

	override fun applyDefaultPlugin() {
		project.plugins.apply("java-library")
	}
}
