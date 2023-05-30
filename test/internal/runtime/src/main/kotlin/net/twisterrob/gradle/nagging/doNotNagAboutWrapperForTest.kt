@file:JvmMultifileClass
@file:JvmName("NaggingUtils")

package net.twisterrob.gradle.nagging

import net.twisterrob.gradle.doNotNagAbout
import net.twisterrob.gradle.isDoNotNagAboutDiagnosticsEnabled
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion

/**
 * Overload for exact message matching.
 *
 * @see net.twisterrob.gradle.doNotNagAbout for more details
 * @see net.twisterrob.gradle.nagging.doNotNagAboutPatternForTest
 * @see net.twisterrob.gradle.nagging.doNotNagAboutStackForTest
 */
fun doNotNagAboutForTest(gradle: Pair<String, String>, agpRegex: String, message: String) {
	if (unsupported(gradle, agpRegex, "Ignoring deprecation: ${message}")) return
	doNotNagAbout(message)
}

/**
 * Overload for exact message matching with stack trace.
 *
 * @see net.twisterrob.gradle.doNotNagAbout for more details
 * @see net.twisterrob.gradle.nagging.doNotNagAboutForTest
 * @see net.twisterrob.gradle.nagging.doNotNagAboutPatternForTest
 */
fun doNotNagAboutStackForTest(gradle: Pair<String, String>, agpRegex: String, message: String, stack: String) {
	if (unsupported(gradle, agpRegex, "Ignoring deprecation: ${message} at ${stack}")) return
	doNotNagAbout(message, stack)
}

/**
 * Overload for more dynamic message matching.
 *
 * This method is named with a suffix of `Pattern` to make it easily available,
 * because method references cannot pick up overloaded methods.
 *
 * @see net.twisterrob.gradle.doNotNagAbout for more details
 * @see net.twisterrob.gradle.nagging.doNotNagAboutForTest
 * @see net.twisterrob.gradle.nagging.doNotNagAboutStackForTest
 */
fun doNotNagAboutPatternForTest(gradle: Pair<String, String>, agpRegex: String, messageRegex: String) {
	if (unsupported(gradle, agpRegex, "Ignoring deprecation regex: ${messageRegex}")) return
	doNotNagAbout(Regex(messageRegex))
}

private fun unsupported(gradle: Pair<String, String>, agpRegex: String, s: String): Boolean {
	val logger = Logging.getLogger(Gradle::class.java)

	val baseGradleVersion = GradleVersion.current().baseVersion
	val (minIncl, maxExcl) = gradle
	if (!(GradleVersion.version(minIncl) <= baseGradleVersion && baseGradleVersion < GradleVersion.version(maxExcl))) {
		if (isDoNotNagAboutDiagnosticsEnabled) {
			val actual = "${GradleVersion.current()}($baseGradleVersion)"
			logger.lifecycle("Gradle version mismatch: ${minIncl} <= ${actual} < ${maxExcl}, not applying:\n\t${s}")
		}
		return true
	}

	val baseAgpVersion = agpVersion.substringBefore("-")
	if (!Regex(agpRegex).matches(baseAgpVersion)) {
		if (isDoNotNagAboutDiagnosticsEnabled) {
			logger.lifecycle("AGP version mismatch: ${agpRegex} does not match ${baseAgpVersion}, not applying:\n\t${s}")
		}
		return true
	}

	// Not wrapped in isNaggingDiagnosticsEnabled, always want to see active ignores.
	logger.lifecycle(s)
	return false
}
