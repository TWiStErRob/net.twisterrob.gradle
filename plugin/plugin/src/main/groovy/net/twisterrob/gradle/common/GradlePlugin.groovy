package net.twisterrob.gradle.common

import org.gradle.api.*
import org.gradle.buildinit.plugins.BuildInitPlugin

class GradlePlugin extends BasePlugin {
	@Override
	void apply(Project target) {
		super.apply(target)

		project.task("debugWrapper") { Task task ->
			def debugFile = project.file('gradled.bat')
			task.description = "Generates a ${debugFile.name} script to start a gradle task in debug mode."
			task.group = BuildInitPlugin.GROUP
			task.outputs.file debugFile
			task.onlyIf {
				project.file('gradlew.bat').exists()
			}
			task.doLast {
				debugFile << GradlePlugin.classLoader.getResourceAsStream('gradled.bat')
			}
		}
	}
}
