package net.twisterrob.gradle.test

import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project
import java.net.JarURLConnection
import java.util.jar.Attributes

class TestPlugin : BaseExposedPlugin() {

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

			return@with
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
