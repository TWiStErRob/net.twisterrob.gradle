package net.twisterrob.gradle.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.tooling.BuildException
import org.gradle.util.GradleVersion
import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

open class BasePlugin : Plugin<Project> {

	@Suppress("PropertyName", "MemberVisibilityCanBePrivate", "VariableNaming")
	protected val LOG: Logger = LoggerFactory.getLogger(javaClass)

	protected lateinit var project: Project

	override fun apply(target: Project) {
		LOG.debug("Applying to ${target}")
		project = target

		checkGradleVersion(GradleVersion.current())
	}

	companion object {
		@VisibleForTesting
		internal fun checkGradleVersion(current: GradleVersion) {
			val required = GradleVersion.version("4.1")
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
