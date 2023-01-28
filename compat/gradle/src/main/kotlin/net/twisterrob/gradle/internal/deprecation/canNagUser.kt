@file:JvmMultifileClass
@file:JvmName("DeprecationUtils")

package net.twisterrob.gradle.internal.deprecation

import org.gradle.util.GradleVersion

fun canNagUser(gradleVersion: GradleVersion): Boolean =
	// STOPSHIP comments
	GradleVersion.version("6.3") <= gradleVersion.baseVersion
			&& gradleVersion.baseVersion <= GradleVersion.version("9.0")
