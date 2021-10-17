package net.twisterrob.gradle.base

import com.google.common.annotations.VisibleForTesting
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.util.GradleVersion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

open class BasePlugin : Plugin<Project> {

	protected lateinit var project: Project
		private set

	@Suppress("PropertyName", "LeakingThis") // keep it consistent with external loggers
	protected var LOG: Logger = LoggerFactory.getLogger(this::class.java)
		private set

	override fun apply(target: Project) {
		this.project = target

		LOG.debug("Applying to ${target}")
		project = target

		checkGradleVersion(GradleVersion.current())
	}

	companion object {

		@VisibleForTesting
		internal fun checkGradleVersion(current: GradleVersion) {
			val required = "4.1"
			if (current < GradleVersion.version(required)) {
				val file = File("gradle/wrapper/gradle-wrapper.properties")
				@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
				throw ProjectConfigurationException(
					"Gradle version ${required}+ is required; the current version is ${current}."
							+ " Edit the distributionUrl in ${file.absolutePath}.",
					null as Throwable?
				)
			}
		}
	}
}
