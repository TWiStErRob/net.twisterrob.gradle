package net.twisterrob.gradle.base

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.create

class GradlePlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.tasks.create<Task>("debugWrapper") {
			val debugFile = project.file("gradled.bat")
			description = "Generates a ${debugFile.name} script to start a Gradle task in debug mode."
			group = "Build Setup"
			outputs.file(debugFile)
			onlyIf {
				project.file("gradlew.bat").exists()
			}
			doLast {
				debugFile.outputStream().use { out ->
					GradlePlugin::class.java.classLoader.getResourceAsStream("gradled.bat").copyTo(out)
				}
			}
		}
	}
}
