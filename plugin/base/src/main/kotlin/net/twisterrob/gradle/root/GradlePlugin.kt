package net.twisterrob.gradle.root

import net.twisterrob.gradle.common.BasePlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.register
import org.gradle.util.GradleVersion

class GradlePlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.tasks.register<Task>("debugWrapper") {
			val debugFile = project.file("gradled.bat")
			val wrapperFile = project.file("gradlew.bat")
			description = "Generates a ${debugFile.name} script to start a Gradle task in debug mode."
			group = "Build Setup"
			if (GradleVersion.version("8.0") <= GradleVersion.current().baseVersion) {
				inputs.file(wrapperFile).skipWhenEmpty()
			} else {
				onlyIf { wrapperFile.exists() }
			}
			outputs.file(debugFile)
			doLast {
				debugFile.outputStream().use { out ->
					val resourceName = "/gradled.bat"
					val gradled = GradlePlugin::class.java.getResourceAsStream(resourceName)
						?: error("Cannot find ${resourceName} on the classpath of ${GradlePlugin::class} to write ${debugFile}.")
					gradled.use { inp -> inp.copyTo(out) }
				}
			}
		}
	}
}
