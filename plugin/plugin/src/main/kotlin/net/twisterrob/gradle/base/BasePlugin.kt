package net.twisterrob.gradle.base

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
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

		checkGradleVersion(project.gradle.gradleVersion)
	}

	companion object {

		internal fun checkGradleVersion(version: String) {
			val pattern = """(?<major>\d+)\.(?<minor>\d+).*""".toRegex()
			val match = pattern.matchEntire(version)
			if (match == null || !(match.groups["major"]!!.value == "4" && 1 <= (match.groups["minor"]!!.value.toInt()))) {
				val file = File("gradle/wrapper/gradle-wrapper.properties")
				val required = "4.1+"
				@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
				throw ProjectConfigurationException(
					"Gradle version ${required} is required; the current version is $version."
							+ " Edit the distributionUrl in ${file.absolutePath}.",
					null
				)
			}
		}
	}
}
