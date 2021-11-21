import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named

/**
 * Disables this warning when `java-gradle-plugin` is applied, but there are no `gradlePlugin.plugins` created.
 * ```
 * > Task :foo:bar:jar
 * :foo:bar:jar: No valid plugin descriptors were found in META-INF/gradle-plugins
 * ```
 */
fun Project.disableGradlePluginValidation() {
	tasks.named<Jar>("jar") {
		@Suppress("LocalVariableName")
		val PluginValidationAction =
			Class.forName("org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin\$PluginValidationAction")
		taskActions.removeIf { PluginValidationAction.isInstance(it) }
	}
}
