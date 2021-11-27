package net.twisterrob.gradle.common

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

interface TaskCreationConfiguration<T : Task> {
	fun preConfigure(project: Project, taskProvider: TaskProvider<T>)

	fun configure(task: T)
}

inline fun <reified T : Task> Project.registerTask(
	name: String,
	configuration: TaskCreationConfiguration<T>
): TaskProvider<T> {
	val provider = this.tasks.register<T>(name) {
		configuration.configure(this)
	}
	configuration.preConfigure(this, provider)
	return provider
}
