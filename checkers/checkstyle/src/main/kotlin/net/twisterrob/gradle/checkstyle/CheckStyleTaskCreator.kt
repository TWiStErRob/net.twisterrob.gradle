package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.VariantTaskCreator
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleReports
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.CustomizableHtmlReport
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Internal
import java.io.File

class CheckStyleTaskCreator(project: Project) : VariantTaskCreator<CheckStyleTask>(
	project,
	"checkstyle",
	"checkstyle",
	CheckStyleTask::class.java,
	CheckStyleExtension::class.java
) {

	@Suppress("CognitiveComplexMethod") // TODEL https://github.com/detekt/detekt/issues/5560
	override fun taskConfigurator(): VariantTaskCreator<CheckStyleTask>.DefaultTaskConfig =
		object : VariantTaskCreator<CheckStyleTask>.DefaultTaskConfig() {

			override fun setupConfigLocations(task: CheckStyleTask) {
				val subConfig = task.project.file("config/checkstyle/checkstyle.xml")
				val rootConfig = task.project.rootProject.file("config/checkstyle/checkstyle.xml")
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
				with(task.reports.customHtml) {
					val xsl = task.project.rootProject.file("config/checkstyle/checkstyle-html.xsl")
					if (xsl.exists()) {
						stylesheet = task.project.resources.text.fromFile(xsl)
					}
				}
			}
		}
}

/**
 * This should be as simple as `html as CustomizableHtmlReport`,
 * but Gradle 5 changed the return type of [Internal] property [CheckstyleReports.getHtml].
 * <5: SingleFileReport getHtml();
 * 5+: CustomizableHtmlReport getHtml();
 * Note: [CustomizableHtmlReport] `extends` [SingleFileReport]
 * This wouldn't be a big problem, except the bytecode generated by Kotlin
 * uses INVOKEINTERFACE where the method return type is explicitly written:
 * ```
 * INVOKEINTERFACE
 * org/gradle/api/plugins/quality/CheckstyleReports.getHtml
 * ()Lorg/gradle/api/reporting/SingleFileReport;
 * ```
 * and this throws when executed on different Gradle versions (e.g. compiled against 4.10.3, executed 5.1.1):
 * ```
 * Caused by: java.lang.NoSuchMethodError:
 * org.gradle.api.plugins.quality.CheckstyleReports.getHtml()Lorg/gradle/api/reporting/SingleFileReport;
 * ```
 */
private val CheckstyleReports.customHtml: CustomizableHtmlReport
	get() {
		val html = this::class.java.getDeclaredMethod("getHtml")
		return html.invoke(this) as CustomizableHtmlReport
	}
