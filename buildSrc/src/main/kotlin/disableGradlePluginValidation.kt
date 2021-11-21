import org.gradle.api.GradleScriptException
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named

fun Project.disableGradlePluginValidation() {
	@Suppress("LocalVariableName")
	tasks.named<Jar>("jar") {
		val PluginValidationAction =
			Class.forName("org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin\$PluginValidationAction")
		val TaskActionWrapper =
			Class.forName("org.gradle.api.internal.AbstractTask\$TaskActionWrapper")
		val actionField = TaskActionWrapper.getDeclaredField("action").apply { isAccessible = true }

		val removed = taskActions.removeIf {
			TaskActionWrapper.isInstance(it) && PluginValidationAction.isInstance(actionField.get(it))
		}
		if (!removed) {
			val actions = taskActions
				.map {
					when {
						TaskActionWrapper.isInstance(it) -> "${it} -> ${actionField.get(it)}"
						else -> it
					}
				}
				.joinToString(prefix = System.lineSeparator(), separator = System.lineSeparator())
			throw GradleScriptException(
				"Tried to remove ${PluginValidationAction} from ${this@named}, but it didn't happen.",
				Throwable("Actions:${actions}") // Satisfy compiler, even though cause can be null.
			)
		}
	}
}
