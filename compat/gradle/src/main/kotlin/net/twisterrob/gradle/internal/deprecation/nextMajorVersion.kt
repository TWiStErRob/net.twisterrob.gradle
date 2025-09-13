@file:JvmMultifileClass
@file:JvmName("DeprecationUtils")

package net.twisterrob.gradle.internal.deprecation

import org.gradle.util.GradleVersion

fun nextMajorVersion(current: GradleVersion): GradleVersion =
	GradleVersion.version("${nextMajorVersionNumber(current)}.0")

fun nextMajorVersionNumber(current: GradleVersion): Int {
	@Suppress("detekt.MagicNumber")
	val nextMajor = when {
		GradleVersion.version("9.0") <= current.baseVersion -> 10
		GradleVersion.version("8.0") <= current.baseVersion -> 9
		GradleVersion.version("7.0") <= current.baseVersion -> 8
		GradleVersion.version("6.3") <= current.baseVersion -> 7
		else -> throw IllegalArgumentException("Unsupported Gradle version: ${current}, willBeRemovedInGradleX doesn't exist yet.")
	}
	return nextMajor
}

/**
 * Version number for deprecation.
 */
fun nextMajorVersionStringForDeprecation(current: GradleVersion): String =
	when {
		GradleVersion.version("9.0") <= current.baseVersion ->
			"Gradle ${nextMajorVersionNumber(current)}"
		else ->
			nextMajorVersion(current).toString()
	}
