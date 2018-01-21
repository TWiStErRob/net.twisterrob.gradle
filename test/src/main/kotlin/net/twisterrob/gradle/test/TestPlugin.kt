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
			add("implementation", localGroovy())
			add("implementation", gradleApi())

			add("testImplementation", gradleTestKit())

			val myManifest = getManifest()

			add("testImplementation", mapOf(
					"group" to myManifest.getValue("Implementation-Vendor"),
					"name" to myManifest.getValue("Implementation-Title"),
					"version" to myManifest.getValue("Implementation-Version")
			))
		}
	}

	private fun getManifest(): Attributes {
		val res = javaClass.getResource("${javaClass.simpleName}.class")
		val conn = res.openConnection() as JarURLConnection
		val mf = conn.manifest
		return mf.mainAttributes
	}
}
