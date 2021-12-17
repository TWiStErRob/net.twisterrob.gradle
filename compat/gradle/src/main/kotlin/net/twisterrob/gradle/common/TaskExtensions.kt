package net.twisterrob.gradle.common

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

val Task.wasLaunchedOnly: Boolean
	get() = project.gradle.startParameter.taskNames == listOf(path)

val Task.wasLaunchedExplicitly: Boolean
	get() = path in project.gradle.startParameter.taskNames

fun TaskProvider<*>.wasLaunchedOnly(project: Project): Boolean =
	project.gradle.startParameter.taskNames == listOf(project.absoluteProjectPath(this.name))

fun TaskProvider<*>.wasLaunchedExplicitly(project: Project): Boolean =
	project.absoluteProjectPath(this.name) in project.gradle.startParameter.taskNames
