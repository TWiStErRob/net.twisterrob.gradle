@file:JvmMultifileClass
@file:JvmName("NaggingUtils")

package net.twisterrob.gradle.nagging

import net.twisterrob.gradle.doNotNagAbout
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion

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
	if (GradleVersion.current().baseVersion != GradleVersion.version(gradle)) return
	if (!Regex(agpRegex).matches(agpVersion)) return

	val logger = Logging.getLogger(Gradle::class.java)
	logger.lifecycle("Ignoring deprecation regex: ${messageRegex}")

	doNotNagAbout(Regex(messageRegex))
}