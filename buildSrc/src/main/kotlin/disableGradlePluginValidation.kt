import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named

fun Project.disableGradlePluginValidation() {
	tasks.named<Jar>("jar") {
		@Suppress("LocalVariableName")
		val PluginValidationAction =
			Class.forName("org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin\$PluginValidationAction")
		taskActions.removeIf { PluginValidationAction.isInstance(it) }
	}
}
