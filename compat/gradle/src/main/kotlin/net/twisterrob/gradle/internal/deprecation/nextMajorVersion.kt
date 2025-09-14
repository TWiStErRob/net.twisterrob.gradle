@file:JvmMultifileClass
@file:JvmName("DeprecationUtils")

package net.twisterrob.gradle.internal.deprecation

import org.gradle.util.GradleVersion

fun nextMajorVersion(target: GradleVersion): GradleVersion =
	GradleVersion.version("${nextMajorVersionNumber(target)}.0")

fun nextMajorVersionNumber(target: GradleVersion): Int {
	@Suppress("detekt.MagicNumber")
	val nextMajor = when {
		GradleVersion.version("9.0") <= target.baseVersion -> 10
		GradleVersion.version("8.0") <= target.baseVersion -> 9
		GradleVersion.version("7.0") <= target.baseVersion -> 8
		GradleVersion.version("6.3") <= target.baseVersion -> 7
		else -> throw IllegalArgumentException(
			"Unsupported Gradle version: ${target}, willBeRemovedInGradleX doesn't exist yet."
		)
	}
	return nextMajor
}

/**
 * Version number for deprecation.
 */
fun nextMajorVersionForDeprecation(target: GradleVersion, current: GradleVersion): String =
	if (GradleVersion.version("9.0") <= current.baseVersion) {
		"Gradle ${nextMajorVersionNumber(target)}"
	} else {
		nextMajorVersion(target).toString()
	}
