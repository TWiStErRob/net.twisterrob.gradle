package net.twisterrob.gradle.test

import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project
import java.net.JarURLConnection
import java.util.jar.Attributes

abstract class TestPlugin : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)
		project.plugins.apply("java-gradle-plugin")

		with(project.dependencies) {
			add("implementation", gradleApi())

			add("testImplementation", gradleTestKit())

			val myManifest = getManifest()
			val selfDependency = mapOf(
				"group" to myManifest.getValue("Implementation-Vendor")!!,
				"name" to myManifest.getValue("Implementation-Title")!!,
				"version" to myManifest.getValue("Implementation-Version")!!
			)
			add("testImplementation", selfDependency)
		}
	}

	private fun getManifest(): Attributes {
		val resourceUrl = javaClass.getResource("${javaClass.simpleName}.class")
			?: error("Cannot find ${javaClass}'s class file in its own ClassLoader.")
		val urlConnection = resourceUrl.openConnection()
			?: error("Couldn't open connection to ${resourceUrl}.")
		val jarConnection = urlConnection as JarURLConnection
		val mf = jarConnection.manifest
			?: error("No manifest entry found in ${jarConnection.jarFileURL}.")
		return mf.mainAttributes
	}
}
