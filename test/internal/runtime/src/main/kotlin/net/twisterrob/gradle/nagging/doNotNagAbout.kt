package net.twisterrob.gradle.nagging

import net.twisterrob.gradle.doNotNagAbout
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion

/**
 * Overload for exact message matching.
 *
 * @see doNotNagAbout for more details
 */
fun doNotNagAbout(gradle: String, agpRegex: String, message: String) {
	if (GradleVersion.current().baseVersion != GradleVersion.version(gradle)) return
	if (!Regex(agpRegex).matches(agpVersion)) return

	val logger = Logging.getLogger(Gradle::class.java)
	logger.lifecycle("Ignoring deprecation: ${message}")

	doNotNagAbout(message)
}
