package net.twisterrob.gradle.java

class JavaPlugin : BaseJavaPlugin() {

	override fun applyDefaultPlugin() {
		project.plugins.apply("java")
	}
}
