package net.twisterrob.gradle.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.tooling.BuildException
import org.gradle.util.GradleVersion
import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

@Suppress("UnnecessaryAbstractClass") // Gradle convention.
abstract class BasePlugin : Plugin<Project> {

	@Suppress(
		"PropertyName", // Keep it consistent with external loggers.
		"MemberVisibilityCanBePrivate",
		"VariableNaming", // Keep it consistent with external loggers.
	)
	protected val LOG: Logger = LoggerFactory.getLogger(this::class.java)

	@Suppress("LateinitUsage") // Too many usages to fix right now. TODO consider removing this field, make dep explicit.
	protected lateinit var project: Project
		private set

	override fun apply(target: Project) {
		this.project = target
		LOG.debug("Applying to ${target}")

		checkGradleVersion(GradleVersion.current())
	}

	companion object {
		@VisibleForTesting
		internal fun checkGradleVersion(current: GradleVersion) {
			val required = GradleVersion.version("7.0")
			if (current.baseVersion < required) {
				val file = File("gradle/wrapper/gradle-wrapper.properties")
				throw BuildException(
					"Gradle version ${required.version}+ is required; the current version is ${current.version}."
							+ " Edit the distributionUrl in ${file.absolutePath}.",
					null
				)
			}
		}
	}
}
