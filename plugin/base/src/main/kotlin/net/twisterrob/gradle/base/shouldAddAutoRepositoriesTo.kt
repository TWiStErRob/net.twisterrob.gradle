package net.twisterrob.gradle.base

import net.twisterrob.gradle.ext.settings
import org.gradle.api.Project
import org.gradle.api.initialization.resolve.RepositoriesMode

@Suppress("UnstableApiUsage")
fun shouldAddAutoRepositoriesTo(project: Project): Boolean =
	@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA") // kotlinc needs this, IDEA doesn't recognize it.
	when (project.settings.dependencyResolutionManagement.repositoriesMode.get()) {
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
