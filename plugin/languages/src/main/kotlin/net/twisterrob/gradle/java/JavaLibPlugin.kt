package net.twisterrob.gradle.java

@Suppress("UnnecessaryAbstractClass") // Gradle convention.
abstract class JavaLibPlugin : BaseJavaPlugin() {

	override fun applyDefaultPlugin() {
		project.plugins.apply("java-library")
	}
}
