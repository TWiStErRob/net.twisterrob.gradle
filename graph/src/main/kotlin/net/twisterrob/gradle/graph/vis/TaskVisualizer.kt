package net.twisterrob.gradle.graph.vis

import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import org.gradle.api.Task
import org.gradle.api.initialization.Settings

interface TaskVisualizer {

	fun showUI(settings: Settings)
	fun initModel(graph: Map<Task, TaskData>)
	fun update(task: Task, result: TaskResult)

	/**
	 * Responsible for
	 *
	 * Closing the UI:
	 *  * hiding if the UI is reusable
	 *  * disposing of it if it needs to be recreated next time
	 *
	 * The cache received in the constructor should be also closed, always, even if the UI failed.
	 */
	fun closeUI()
}
