package net.twisterrob.gradle.build.publishing

import net.twisterrob.gradle.build.dsl.gradlePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.named
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.gradle.plugin.devel.tasks.ValidatePlugins

abstract class GradlePluginValidationPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.withId("org.gradle.java-gradle-plugin") {
			configureValidatePlugins(project.tasks)
			project.plugins.withId("org.gradle.maven-publish") {
				// TODO hook up validation to publishing, so it only executes when relevant.
				project.afterEvaluate {
					validateGradlePlugin(this, gradlePlugin)
				}
			}
		}
	}

	/**
	 * @see JavaGradlePluginPlugin.VALIDATE_PLUGINS_TASK_NAME
	 */
	private fun configureValidatePlugins(tasks: TaskContainer) {
		tasks.named<ValidatePlugins>("validatePlugins").configure {
			ignoreFailures.set(false)
			failOnWarning.set(true)
			enableStricterValidation.set(true)
		}
	}

	@Suppress("UnstableApiUsage")
	private fun validateGradlePlugin(project: Project, gradlePlugin: GradlePluginDevelopmentExtension) {
		if (!gradlePlugin.website.isPresent) {
			error("${project} missing website for Gradle Plugin publications.")
		}
		if (!gradlePlugin.vcsUrl.isPresent) {
			error("${project} missing website for Gradle Plugin publications.")
		}
		gradlePlugin.plugins.configureEach {
			validatePlugin(this)
		}
	}

	@Suppress("UnstableApiUsage")
	private fun validatePlugin(plugin: PluginDeclaration) {
		plugin.id ?: error("Plugin ID for ${plugin.name} is not set.")
		plugin.displayName ?: error("Plugin Display Name for ${plugin.id} is not set.")
		plugin.description ?: error("Plugin Description for ${plugin.id} is not set.")
		plugin.implementationClass ?: error("Plugin implementation for ${plugin.id} is not set.")
		if (plugin.tags.getOrElse(emptySet()).isEmpty()) {
			error("Plugin Tags for ${plugin.id} are not set.")
		}
	}
}
