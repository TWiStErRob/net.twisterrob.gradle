@file:JvmMultifileClass
@file:JvmName("NaggingUtils")

package net.twisterrob.gradle.nagging

import net.twisterrob.gradle.doNotNagAbout
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion

/**
 * Overload for exact message matching.
 *
 * @see doNotNagAbout for more details
 * @see net.twisterrob.gradle.nagging.doNotNagAboutPattern
 */
fun doNotNagAbout(gradle: String, agpRegex: String, message: String) {
	if (unsupported(gradle, agpRegex, "Ignoring deprecation: ${message}")) return
	doNotNagAbout(message)
}

/**
 * Overload for exact message matching with stack trace.
 *
 * @see doNotNagAbout for more details
 * @see net.twisterrob.gradle.nagging.doNotNagAbout
 */
fun doNotNagAbout(gradle: String, agpRegex: String, message: String, stack: String) {
	if (unsupported(gradle, agpRegex, "Ignoring deprecation: ${message} at ${stack}")) return
	doNotNagAbout(message, stack)
}

/**
 * Overload for more dynamic message matching.
 *
 * This method is named with a suffix of `Pattern` to make it easily available,
 * because method references cannot pick up overloaded methods.
 *
 * @see doNotNagAbout for more details
 * @see net.twisterrob.gradle.nagging.doNotNagAbout
 */
fun doNotNagAboutPattern(gradle: String, agpRegex: String, messageRegex: String) {
	if (unsupported(gradle, agpRegex, "Ignoring deprecation regex: ${messageRegex}")) return
	doNotNagAbout(Regex(messageRegex))
}

private fun unsupported(gradle: String, agpRegex: String, s: String): Boolean {
	if (GradleVersion.current().baseVersion != GradleVersion.version(gradle)) {
		return true
	}

	if (!Regex(agpRegex).matches(agpVersion)) {
		return true
	}

	val logger = Logging.getLogger(Gradle::class.java)
	logger.lifecycle(s)
	return false
}
