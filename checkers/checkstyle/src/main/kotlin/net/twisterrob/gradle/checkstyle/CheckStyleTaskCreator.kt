package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.VariantTaskCreator
import org.gradle.api.Project

class CheckStyleTaskCreator(project: Project) : VariantTaskCreator<CheckStyleTask>(
	project,
	"checkstyle",
	"checkstyle",
	CheckStyleTask::class.java,
	CheckStyleExtension::class.java
) {

	@Suppress("detekt.CognitiveComplexMethod") // TODEL https://github.com/detekt/detekt/issues/5560
	override fun taskConfigurator(): VariantTaskCreator<CheckStyleTask>.DefaultTaskConfig =
		object : VariantTaskCreator<CheckStyleTask>.DefaultTaskConfig() {

			override fun setupConfigLocations(task: CheckStyleTask) {
				val subConfig = task.project.file("config/checkstyle/checkstyle.xml")
				val rootConfig = task.project.rootDir.resolve("config/checkstyle/checkstyle.xml")
				if (!task.configFile.exists() || (subConfig.exists() && rootConfig.exists())) {
					if (!subConfig.exists() && !rootConfig.exists()) {
						task.doFirst("Warn about missing configuration files.") {
							task.logger.warn(
								"""
									|While auto-configuring configFile for ${task}, there was no configuration found at:
									|	rootProject=${rootConfig}
									|	subProject=${subConfig}
									|	task=${task.configFile}
									|	and there's no configuration location set in Gradle build files either.
								""".trimMargin()
							)
						}
					}
					// if both of them exists, take the subproject's one instead of the rootProject's
					if (subConfig.exists()) {
						task.configFile = subConfig
					} else if (rootConfig.exists()) {
						task.configFile = rootConfig
					}
				}
				task.configDirectory.set(task.configFile.parentFile)
			}

			override fun setupReports(task: CheckStyleTask, suffix: String?) {
				super.setupReports(task, suffix)
				with(task.reports.html) {
					val xsl = task.project.rootProject.file("config/checkstyle/checkstyle-html.xsl")
					if (xsl.exists()) {
						stylesheet = task.project.resources.text.fromFile(xsl)
					}
				}
			}
		}
}
