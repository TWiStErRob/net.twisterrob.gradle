package net.twisterrob.gradle.java

abstract class JavaPlugin : BaseJavaPlugin() {

	override fun applyDefaultPlugin() {
		project.plugins.apply("java")
	}
}
