package net.twisterrob.gradle.java

abstract class JavaLibPlugin : BaseJavaPlugin() {

	override fun applyDefaultPlugin() {
		project.plugins.apply("java-library")
	}
}
