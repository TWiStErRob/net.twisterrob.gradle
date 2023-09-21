package net.twisterrob.gradle.java

@Suppress("UnnecessaryAbstractClass") // Gradle convention.
abstract class JavaPlugin : BaseJavaPlugin() {

	override fun applyDefaultPlugin() {
		project.plugins.apply("java")
	}
}
