package net.twisterrob.gradle.nagging

import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion

/**
 * Overload for more dynamic message matching.
 *
 * This method is named with a suffix of `Pattern` to make it easily available,
 * because method references cannot pick up overloaded methods.
 *
 * @see _doNotNagAboutPattern for more details
 */
fun doNotNagAboutPattern(gradle: String, agpRegex: String, messageRegex: String) {
	if (GradleVersion.current().baseVersion == GradleVersion.version(gradle)) {
		if (Regex(agpRegex) matches agpVersion) {
			val logger = Logging.getLogger(Gradle::class.java)
			logger.lifecycle("Ignoring deprecation: ${messageRegex}")
			_doNotNagAboutPattern(Regex(messageRegex))
		}
	}
}
