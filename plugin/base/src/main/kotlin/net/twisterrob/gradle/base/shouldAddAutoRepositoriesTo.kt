package net.twisterrob.gradle.base

import net.twisterrob.gradle.ext.settings
import org.gradle.api.Project
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.util.GradleVersion

fun shouldAddAutoRepositoriesTo(project: Project): Boolean {
	if (GradleVersion.version("6.8") <= GradleVersion.current()) {
		@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA", "UnstableApiUsage")
		return when (project.settings.dependencyResolutionManagement.repositoriesMode.get()) {
			RepositoriesMode.PREFER_PROJECT -> {
				// Project is using defaults, or explicitly preferring these repositories.
				true
			}
			RepositoriesMode.PREFER_SETTINGS -> {
				// Automatic repositories will be ignored, don't even try.
				false
			}
			RepositoriesMode.FAIL_ON_PROJECT_REPOS -> {
				// Automatic repositories will fail the build, respect the user.
				false
			}
		}
	} else {
		// Legacy behavior is the same as RepositoriesMode.PREFER_PROJECT,
		// because there's no way to define in settings.gradle.
		return true
	}
}
