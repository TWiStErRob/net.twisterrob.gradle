@file:JvmMultifileClass
@file:JvmName("DeprecationUtils")

package net.twisterrob.gradle.internal.deprecation

import org.gradle.util.GradleVersion

fun canNagUser(gradleVersion: GradleVersion): Boolean =
	// Gradle 6.3+ has a nagging mechanism,
	// at that point there were some major changes to the internal DeprecationMessageBuilder.
	// I think it's not worth going back more to support older versions with nagUserWith.
	// Give it an upper limit too, as we don't know if it will keep working past Gradle 10.
	gradleVersion.baseVersion in GradleVersion.version("6.3")..GradleVersion.version("10.0")
