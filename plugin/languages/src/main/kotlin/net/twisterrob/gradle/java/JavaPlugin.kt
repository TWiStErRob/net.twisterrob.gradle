package net.twisterrob.gradle.java

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class JavaPlugin : BaseJavaPlugin() {

	override fun applyDefaultPlugin() {
		project.plugins.apply("java")
	}
}
