package net.twisterrob.gradle.graph.vis

import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import org.gradle.api.Task
import org.gradle.api.initialization.Settings

class DummyTaskVisualizer : TaskVisualizer {

	override fun showUI(settings: Settings) {
		// do nothing
	}

	override fun initModel(graph: Map<Task, TaskData>) {
		// do nothing
	}

	override fun update(task: Task, result: TaskResult) {
		// do nothing
	}

	override fun closeUI() {
		// do nothing
	}
}
