package net.twisterrob.gradle.nagging

import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion

/**
 * Overload for exact message matching.
 *
 * @see _doNotNagAboutPattern for more details
 */
fun doNotNagAbout(gradle: String, agpRegex: String, message: String) {
	if (GradleVersion.current().baseVersion == GradleVersion.version(gradle)) {
		if (Regex(agpRegex) matches agpVersion) {
			val logger = Logging.getLogger(Gradle::class.java)
			logger.lifecycle("Ignoring deprecation: ${message}")
			_doNotNagAboutPattern(Regex.fromLiteral(message))
		}
	}
}
