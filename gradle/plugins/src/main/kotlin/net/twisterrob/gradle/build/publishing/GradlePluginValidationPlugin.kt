package net.twisterrob.gradle.build.publishing

import gradlePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.named
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.gradle.plugin.devel.tasks.ValidatePlugins

class GradlePluginValidationPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.withId("org.gradle.java-gradle-plugin") {
			/**
			 * @see JavaGradlePluginPlugin.VALIDATE_PLUGINS_TASK_NAME
			 */
			val validatePlugins = project.tasks.named<ValidatePlugins>("validatePlugins")
			validatePlugins.configure {
				ignoreFailures.set(false)
				failOnWarning.set(true)
				enableStricterValidation.set(true)
			}

			project.plugins.withId("org.gradle.maven-publish") {
				// TODO hook up validation to publishing, so it only executes when relevant.
				project.afterEvaluate {
					@Suppress("UnstableApiUsage")
					project.gradlePlugin.apply {
						if (!website.isPresent) {
							error("$project missing website for Gradle Plugin publications.")
						}
						if (!vcsUrl.isPresent) {
							error("$project missing website for Gradle Plugin publications.")
						}
						plugins.configureEach plugin@{
							val plugin = this@plugin
							plugin.id ?: error("Plugin ID for ${plugin.name} is not set.")
							plugin.displayName ?: error("Plugin Display Name for ${plugin.id} is not set.")
							plugin.description ?: error("Plugin Description for ${plugin.id} is not set.")
							plugin.implementationClass ?: error("Plugin implementation for ${plugin.id} is not set.")
							if (plugin.tags.getOrElse(emptySet()).isEmpty()) {
								error("Plugin Tags for ${plugin.id} are not set.")
							}
						}
					}
				}
			}
		}
	}
}
