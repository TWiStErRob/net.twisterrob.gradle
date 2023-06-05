package net.twisterrob.gradle.quality.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

@Suppress("UnnecessaryAbstractClass") // Gradle convention.
@UntrackedTask(because = "It is used to inspect Gradle state, output is console.")
abstract class VersionsTask : DefaultTask() {

	@get:Input
	internal abstract val gradleVersion: Property<String>

	@get:Input
	internal abstract val checkstyleVersion: Property<String>

	@get:Input
	internal abstract val pmdVersion: Property<String>

	init {
		gradleVersion.convention(project.provider { project.gradle.gradleVersion })
		checkstyleVersion.convention(project.provider { project.getVersion<CheckstyleExtension>("checkstyle") })
		pmdVersion.convention(project.provider { project.getVersion<PmdExtension>("pmd") })
	}

	@TaskAction
	fun printVersions() {
		logger.quiet(
			"""
				Gradle version: ${gradleVersion.get()}
				Checkstyle version: ${checkstyleVersion.get()}
				PMD version: ${pmdVersion.get()}
			""".trimIndent()
		)
	}
}

private inline fun <reified T : CodeQualityExtension> Project.getVersion(pluginName: String): String =
	project.extensions.findByType(T::class.java)?.toolVersion ?: "'${pluginName}' plugin not applied"
