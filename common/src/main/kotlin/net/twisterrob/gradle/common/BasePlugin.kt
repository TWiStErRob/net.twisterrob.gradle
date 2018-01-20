package net.twisterrob.gradle.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.tooling.BuildException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

open class BasePlugin : Plugin<Project> {

	@Suppress("PropertyName", "MemberVisibilityCanBePrivate")
	protected val LOG: Logger = LoggerFactory.getLogger(javaClass)

	protected lateinit var project: Project

	override fun apply(target: Project) {
		LOG.debug("Applying to ${target}")
		project = target

		val match = """(?<major>\d+)\.(?<minor>\d+).*""".toRegex().matchEntire(project.gradle.gradleVersion)
		// TODO Kotlin !(match.groups["major"]!!.value == "4",
		// test classpath doesn't recognize JDK8 extensions
		// because PlatformImplementations is loaded from the Gradle distribution,
		// and the IMPLEMENTATIONS ClassLoader is trying from the wrong place. (see Class.forName: getCallerClass)
		if (match == null || !(match.groups[1]!!.value == "4" && 1 <= match.groups[2]!!.value.toInt())) {
			val file = File("gradle" + File.separator + "wrapper" + File.separator + "gradle-wrapper.properties")
			val required = "4.1+"
			throw BuildException(
					"Gradle version ${required} is required; the current version is ${project.gradle.gradleVersion}."
							+ " Edit the distributionUrl in ${file.absolutePath}.", null
			)
		}
	}
}
