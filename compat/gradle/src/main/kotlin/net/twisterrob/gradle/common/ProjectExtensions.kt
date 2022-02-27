package net.twisterrob.gradle.common

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal

val Project.settings: Settings
	get() = (gradle as GradleInternal).settings
