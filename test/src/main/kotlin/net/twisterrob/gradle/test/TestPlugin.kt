package net.twisterrob.gradle.test

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.JarURLConnection
import java.util.jar.Attributes

class TestPlugin : Plugin<Project> {

	override fun apply(project: Project) {
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
		val res = javaClass.getResource("${javaClass.simpleName}.class")
			?: error("Cannot find ${javaClass}'s class file in its own ClassLoader.")
		val conn = res.openConnection() as JarURLConnection
		val mf = conn.manifest
			?: error("No manifest entry found in ${conn.jarFileURL}.")
		return mf.mainAttributes
	}
}
