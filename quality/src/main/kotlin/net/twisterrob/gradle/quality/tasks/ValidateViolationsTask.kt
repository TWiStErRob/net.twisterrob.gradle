package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.QualityPlugin.Companion.REPORT_CONSOLE_TASK_NAME
import net.twisterrob.gradle.quality.QualityPlugin.Companion.REPORT_HTML_TASK_NAME
import net.twisterrob.gradle.quality.Violations
import org.gradle.api.GradleException
import org.gradle.api.tasks.CacheableTask

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
@CacheableTask
abstract class ValidateViolationsTask : BaseViolationsTask() {

	override fun processViolations(violations: Grouper.Start<Violations>) {
		if (violations.list.any { it.violations.orEmpty().isNotEmpty() }) {
			val message = buildString {
				append("There were violations.")
				val hasConsole = project.gradle.taskGraph.hasTask(":$REPORT_CONSOLE_TASK_NAME")
				val hasHtml = project.gradle.taskGraph.hasTask(":$REPORT_HTML_TASK_NAME")
				if (hasConsole || hasHtml) {
					// No message, it's already going to execute.
				} else {
					append("To get a full breakdown and listing, execute $REPORT_CONSOLE_TASK_NAME or $REPORT_HTML_TASK_NAME.")
				}
			}
			throw GradleException(message)
		}
	}
}
