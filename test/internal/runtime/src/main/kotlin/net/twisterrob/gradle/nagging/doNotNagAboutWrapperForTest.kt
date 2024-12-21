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
fun doNotNagAboutForTest(gradle: Pair<String, String>, agp: Pair<String, String>, message: String) {
	if (unsupported(gradle, agp, "Ignoring deprecation: ${message}")) return
	doNotNagAbout(message)
}

/**
 * Overload for exact message matching with stack trace.
 *
 * @see net.twisterrob.gradle.doNotNagAbout for more details
 * @see net.twisterrob.gradle.nagging.doNotNagAboutForTest
 * @see net.twisterrob.gradle.nagging.doNotNagAboutPatternForTest
 */
fun doNotNagAboutStackForTest(gradle: Pair<String, String>, agp: Pair<String, String>, message: String, stack: String) {
	if (unsupported(gradle, agp, "Ignoring deprecation: ${message} at ${stack}")) return
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
fun doNotNagAboutPatternForTest(gradle: Pair<String, String>, agp: Pair<String, String>, messageRegex: String) {
	if (unsupported(gradle, agp, "Ignoring deprecation regex: ${messageRegex}")) return
	doNotNagAbout(Regex(messageRegex))
}

/**
 * @param gradle Inclusive to exclusive version range for Gradle.
 * @param agp Inclusive to exclusive version range for Android Gradle Plugin.
 * @param message Error message to log.
 */
@Suppress("detekt.NamedArguments") // Variable names are clear enough.
private fun unsupported(gradle: Pair<String, String>, agp: Pair<String, String>, message: String): Boolean {
	val logger = Logging.getLogger(Gradle::class.java)

	/**
	 * Uses [GradleVersion] to compare versions, even for AGP, in the end it's all just semantic versions.
	 */
	fun matchesVersion(type: String, fullVersion: String, baseVersion: String, minIncl: String, maxExcl: String): Boolean {
		val comparedVersion = GradleVersion.version(baseVersion)
		if (GradleVersion.version(minIncl) <= comparedVersion && comparedVersion < GradleVersion.version(maxExcl)) {
			return true
		}
		if (isDoNotNagAboutDiagnosticsEnabled) {
			val actual = "${baseVersion}(${fullVersion})"
			logger.lifecycle("${type} version mismatch: ${minIncl} <= ${actual} < ${maxExcl}, not applying:\n\t${message}")
		}
		return false
	}

	val fullGradleVersion = GradleVersion.current().version
	val baseGradleVersion = GradleVersion.current().baseVersion.version
	if (!matchesVersion("Gradle", fullGradleVersion, baseGradleVersion, gradle.first, gradle.second)) {
		return true
	}

	val fullAgpVersion = agpVersion
	val baseAgpVersion = agpVersion.substringBefore("-")
	if (!matchesVersion("AGP", fullAgpVersion, baseAgpVersion, agp.first, agp.second)) {
		return true
	}

	// Not wrapped in isNaggingDiagnosticsEnabled, always want to see active ignores.
	logger.lifecycle(message)
	return false
}
