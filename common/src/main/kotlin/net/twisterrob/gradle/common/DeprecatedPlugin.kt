package net.twisterrob.gradle.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.internal.deprecation.DeprecationLogger
import org.gradle.internal.deprecation.DeprecationMessageBuilder
import org.gradle.util.GradleVersion

open class DeprecatedProjectPlugin(
	private val oldName: String,
	private val newName: String,
) : Plugin<Project> {

	override fun apply(project: Project) {
		nagUserWithPluginDeprecation(project.project.logger, oldName, newName)
		project.plugins.apply(newName)
	}
}

open class DeprecatedSettingsPlugin(
	private val oldName: String,
	private val newName: String,
) : Plugin<Settings> {

	override fun apply(settings: Settings) {
		nagUserWithPluginDeprecation(Logging.getLogger(Settings::class.java), oldName, newName)
		settings.plugins.apply(newName)
	}
}

private fun nagUserWithPluginDeprecation(logger: Logger, oldName: String, newName: String) {
	if (GradleVersion.version("6.3") <= GradleVersion.current().baseVersion
		&& GradleVersion.current().baseVersion <= GradleVersion.version("9.0")
	) {
		gradleInternalNagging(oldName, newName)
	} else {
		logger.warn("Plugin ${oldName} is deprecated, please use ${newName} instead.")
	}
}

private fun gradleInternalNagging(oldName: String, newName: String) {
	// Hook into the Gradle deprecation system so that org.gradle.warning.mode=fail catches it.
	// It will print "This is scheduled to be removed in Gradle X.0.",
	// but that's acceptable considering the benefits.
	val builder: DeprecationMessageBuilder<*> = DeprecationLogger
		.deprecatePlugin(oldName)
		.replaceWith(newName)
	builder.willBeRemovedInGradleNextMajor()
	DeprecationLogger::class.java
		.getDeclaredMethod("nagUserWith", DeprecationMessageBuilder::class.java, Class::class.java)
		.apply { isAccessible = true }
		.invoke(null, builder, Class.forName("net.twisterrob.gradle.common.DeprecatedPluginKt"))
}

private fun <T : DeprecationMessageBuilder<T>> DeprecationMessageBuilder<T>.willBeRemovedInGradleNextMajor() {
	@Suppress("MagicNumber")
	val nextMajor = when {
		GradleVersion.current().baseVersion >= GradleVersion.version("8.0") -> 9
		GradleVersion.current().baseVersion >= GradleVersion.version("7.0") -> 8
		GradleVersion.current().baseVersion >= GradleVersion.version("6.3") -> 7
		else -> error("Unsupported Gradle version: ${GradleVersion.current()}, willBeRemovedInGradleX doesn't exist yet.")
	}
	DeprecationMessageBuilder::class.java
		.getDeclaredMethod("willBeRemovedInGradle$nextMajor")
		.apply { isAccessible = true }
		.invoke(this)
}
