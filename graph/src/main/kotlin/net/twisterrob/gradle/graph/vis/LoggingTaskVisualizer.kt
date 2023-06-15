package net.twisterrob.gradle.graph.vis

import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.slf4j.Logger

class LoggingTaskVisualizer(
	private val wrapped: TaskVisualizer,
	private val logger: Logger,
) : TaskVisualizer {

	override fun showUI(settings: Settings) {
		logger.debug("showUI({})", settings)
		wrapped.showUI(settings)
	}

	override fun initModel(graph: Map<Task, TaskData>) {
		logger.debug("initModel({})", graph)
		wrapped.initModel(graph)
	}

	override fun update(task: Task, result: TaskResult) {
		logger.debug("update({}, {})", task, result)
		wrapped.update(task, result)
	}

	override fun closeUI() {
		logger.debug("closeUI()")
		wrapped.closeUI()
	}
}
