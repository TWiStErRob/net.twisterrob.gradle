@file:JvmMultifileClass
@file:JvmName("DeprecationUtils")

package net.twisterrob.gradle.internal.deprecation

import org.gradle.util.GradleVersion

fun nextMajorVersion(current: GradleVersion): GradleVersion =
	GradleVersion.version("${nextMajorVersionNumber(current)}.0")

fun nextMajorVersionNumber(current: GradleVersion): Int {
	@Suppress("detekt.MagicNumber")
	val nextMajor = when {
		current.baseVersion >= GradleVersion.version("9.0") -> 10
		current.baseVersion >= GradleVersion.version("8.0") -> 9
		current.baseVersion >= GradleVersion.version("7.0") -> 8
		current.baseVersion >= GradleVersion.version("6.3") -> 7
		else -> error("Unsupported Gradle version: ${current}, willBeRemovedInGradleX doesn't exist yet.")
	}
	return nextMajor
}
