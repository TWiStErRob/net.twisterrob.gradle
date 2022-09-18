@file:JvmMultifileClass
@file:JvmName("GradleUtils")

package net.twisterrob.gradle

import org.gradle.api.Project

/**
 * Generate a file-name-friendly identifier for a [Project].
 * Better than [Project.getName], because that wouldn't be unique, but worse than [Project.getPath],
 * because it could be ambiguous in some edge cases (:a:b and :a-b will have the same [slug]).
 */
val Project.slug: String
	get() =
		if (this == rootProject) {
			// Special case, since the other case removes leading ":".
			"root"
		} else {
			this
				.path // Project's Gradle path -> ":a:b".
				.removePrefix(":") // Remove first colon -> "a:b".
				.replace(":", "-") // Convert to Maven coordinate convention -> "a-b".
		}
