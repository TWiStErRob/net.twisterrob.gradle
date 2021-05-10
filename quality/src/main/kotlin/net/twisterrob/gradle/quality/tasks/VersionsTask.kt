@file:Suppress("DEPRECATION")

package net.twisterrob.gradle.quality.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.plugins.quality.FindBugsExtension
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.tasks.TaskAction

open class VersionsTask : DefaultTask() {

	init {
		outputs.upToDateWhen { false }
	}

	@Suppress("unused")
	@TaskAction
	fun printVersions() {
		println(
			"""
			Gradle version: ${project.gradle.gradleVersion}
			Checkstyle version: ${getVersion("checkstyle", CheckstyleExtension::class.java)}
			PMD version: ${getVersion("pmd", PmdExtension::class.java)}
			FindBugs version: ${@Suppress("DEPRECATION") getVersion("findbugs", FindBugsExtension::class.java)}
			""".trimIndent()
		)
	}

	private fun getVersion(pluginName: String, type: Class<out CodeQualityExtension>) =
		project.extensions.findByType(type)?.toolVersion ?: "'${pluginName}' plugin not applied"
}
