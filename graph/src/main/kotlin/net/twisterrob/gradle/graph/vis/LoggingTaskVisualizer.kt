package net.twisterrob.gradle.graph.vis

import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import org.gradle.api.Task
import org.gradle.api.initialization.Settings

class LoggingTaskVisualizer constructor(
	private val wrapped: TaskVisualizer
) : TaskVisualizer {

	override fun showUI(project: Settings) {
		System.out.printf("showUI(%s)\n", project)
		wrapped.showUI(project)
	}

	override fun initModel(graph: Map<Task, TaskData>) {
		System.out.printf("initModel(%s)\n", graph)
		wrapped.initModel(graph)
	}

	override fun update(task: Task, result: TaskResult) {
		System.out.printf("update(%s, %s)\n", task, result)
		wrapped.update(task, result)
	}

	override fun closeUI() {
		System.out.printf("closeUI()\n")
		wrapped.closeUI()
	}
}
