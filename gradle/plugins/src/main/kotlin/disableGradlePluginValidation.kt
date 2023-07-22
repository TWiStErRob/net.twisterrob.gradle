@file:Suppress("MissingPackageDeclaration") // In default package so it "just works" in build.gradle.kts files.

import org.gradle.api.GradleScriptException
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

/**
 * Disables this warning when `java-gradle-plugin` is applied, but there are no `gradlePlugin.plugins` created.
 * ```
 * > Task :foo:bar:jar
 * :foo:bar:jar: No valid plugin descriptors were found in META-INF/gradle-plugins
 * ```
 */
fun GradlePluginDevelopmentExtension.disableGradlePluginValidation(project: Project) {
	project.afterEvaluate {
		val gradlePluginPlugins = this@disableGradlePluginValidation.plugins
		check(gradlePluginPlugins.isEmpty()) {
			"There are plugins declared in ${this}, don't disable plugin validation: ${gradlePluginPlugins.map { it.id }}"
		}
	}

	@Suppress("LocalVariableName", "VariableNaming") // Reflection.
	project.tasks.named<Jar>("jar") {
		val PluginValidationAction =
			Class.forName("org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin\$PluginValidationAction")
		val TaskActionWrapper =
			Class.forName("org.gradle.api.internal.AbstractTask\$TaskActionWrapper")
		val actionField = TaskActionWrapper.getDeclaredField("action").apply { isAccessible = true }

		val isRemoved = taskActions.removeIf {
			TaskActionWrapper.isInstance(it) && PluginValidationAction.isInstance(actionField.get(it))
		}
		if (!isRemoved) {
			val actions = taskActions
				.joinToString(prefix = System.lineSeparator(), separator = System.lineSeparator()) { action ->
					if (TaskActionWrapper.isInstance(action)) {
						"${action} -> ${actionField.get(action)}"
					} else {
						action.toString()
					}
				}
			throw GradleScriptException(
				"Tried to remove ${PluginValidationAction} from ${this@named}, but it didn't happen.",
				Throwable("Actions:${actions}") // Satisfy compiler, even though cause can be null.
			)
		}
	}
}
