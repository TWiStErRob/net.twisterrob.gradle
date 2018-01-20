package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.common.VariantTaskCreator
import org.gradle.api.Project
import java.io.File

class PmdTaskCreator(project: Project) : VariantTaskCreator<PmdTask>(
		project, "pmd", "pmd", PmdTask::class.java
) {

	override fun taskConfigurator() = object : VariantTaskCreator<PmdTask>.DefaultTaskConfig() {

		override fun setupConfigLocations(task: PmdTask) {
			task.ruleSets = listOf() // default is java-basic
			val rootConfig = task.project.rootProject.file("config/pmd/pmd.xml")
			val subConfig = task.project.file("config/pmd/pmd.xml")
			val config : File? = listOf(subConfig, rootConfig).firstOrNull { it.exists() }
			if (config != null) {
				task.ruleSetFiles += task.project.files(config)
			} else if (task.ruleSetFiles.files.isEmpty()) {
				task.logger.warn("""\
					While configuring ${task} no configuration found at:
						${rootConfig}
						${subConfig}
				""".trimIndent())
			}
		}
	}
}
