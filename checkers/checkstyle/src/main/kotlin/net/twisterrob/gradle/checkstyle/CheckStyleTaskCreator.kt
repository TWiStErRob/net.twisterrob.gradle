package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.VariantTaskCreator
import org.gradle.api.Project
import org.gradle.api.reporting.CustomizableHtmlReport

class CheckStyleTaskCreator(project: Project) : VariantTaskCreator<CheckStyleTask>(
		project, "checkstyle", "checkstyle", CheckStyleTask::class.java
) {

	override fun taskConfigurator() = object : VariantTaskCreator<CheckStyleTask>.DefaultTaskConfig() {

		override fun setupConfigLocations(task: CheckStyleTask) {
			if (!task.configFile.exists()) {
				val rootConfig = task.project.rootProject.file("config/checkstyle/checkstyle.xml")
				if (!rootConfig.exists()) {
					task.logger.warn("""
						While configuring ${task} no configuration found at:
							${rootConfig}
							${task.configFile}
							and there's no valid location set in Gradle build files either.
					""".trimIndent())
				}
				task.configFile = rootConfig
			}
			task.setConfigDir(task.project.provider { task.configFile.parentFile })
		}

		override fun setupReports(task: CheckStyleTask, suffix: String?) {
			super.setupReports(task, suffix)
			with(task.reports.html as CustomizableHtmlReport) {
				val xsl = task.project.rootProject.file("config/checkstyle/checkstyle-html.xsl")
				if (xsl.exists()) {
					stylesheet = task.project.resources.text.fromFile(xsl)
				}
			}
		}
	}
}
