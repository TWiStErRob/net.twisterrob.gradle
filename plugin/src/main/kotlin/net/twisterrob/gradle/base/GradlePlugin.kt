package net.twisterrob.gradle.base

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.register

class GradlePlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.tasks.register<Task>("debugWrapper") {
			val debugFile = project.file("gradled.bat")
			description = "Generates a ${debugFile.name} script to start a Gradle task in debug mode."
			group = "Build Setup"
			outputs.file(debugFile)
			onlyIf {
				project.file("gradlew.bat").exists()
			}
			doLast {
				debugFile.outputStream().use { out ->
					val resourceName = "gradled.bat"
					val gradled = GradlePlugin::class.java.classLoader.getResourceAsStream(resourceName)
						?: error("Cannot find $resourceName on the classpath of ${GradlePlugin::class}.")
					gradled.use { inp -> inp.copyTo(out) }
				}
			}
		}
	}
}
