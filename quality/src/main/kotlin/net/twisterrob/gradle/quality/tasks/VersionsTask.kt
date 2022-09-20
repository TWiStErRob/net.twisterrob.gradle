package net.twisterrob.gradle.quality.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

@UntrackedTask(because = "It is used to inspect Gradle state, output is console.")
abstract class VersionsTask : DefaultTask() {

	init {
		outputs.upToDateWhen { false }
	}

	@TaskAction
	fun printVersions() {
		logger.quiet(
			"""
			Gradle version: ${project.gradle.gradleVersion}
			Checkstyle version: ${getVersion("checkstyle", CheckstyleExtension::class.java)}
			PMD version: ${getVersion("pmd", PmdExtension::class.java)}
			""".trimIndent()
		)
	}

	private fun getVersion(pluginName: String, type: Class<out CodeQualityExtension>): String =
		project.extensions.findByType(type)?.toolVersion ?: "'${pluginName}' plugin not applied"
}
