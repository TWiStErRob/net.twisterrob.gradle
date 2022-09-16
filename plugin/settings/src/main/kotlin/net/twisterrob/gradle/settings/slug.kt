package net.twisterrob.gradle.settings

import org.gradle.api.Project

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
