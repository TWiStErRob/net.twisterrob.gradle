package net.twisterrob.gradle.java

class JavaLibPlugin : BaseJavaPlugin() {

	override fun applyDefaultPlugin() {
		project.plugins.apply("java-library")
	}
}
