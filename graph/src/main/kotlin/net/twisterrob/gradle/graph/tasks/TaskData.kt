package net.twisterrob.gradle.graph.tasks

import org.gradle.api.Task
import java.util.TreeMap
import java.util.TreeSet

class TaskData(
	val task: Task,
) : Comparable<TaskData> {

	var type: TaskType = TaskType.Unknown
	var state: TaskResult? = null
	val deps: MutableSet<TaskData> = TreeSet()
	val depsDirect: MutableSet<TaskData> = TreeSet()
	val depsImplicit: MutableMap<TaskData, List<TaskData>> = TreeMap()

	/**
	 * Optimization to prevent <code>Set&lt;TaskData&gt; done</code> in algorithms.
	 * It's simpler to set a boolean flag than to construct complex data structures, though not as nice.
	 *
	 * @see #resetVisited
	 */
	var visited: Boolean = false

	override fun compareTo(other: TaskData): Int =
		this.task.compareTo(other.task)

	override fun toString(): String =
		"{%s, type=%s, state=%s, deps=%s}".format(task.path, type, state, deps.map { it.task.path })

	companion object {

		/**
		 * @see #visited
		 */
		fun resetVisited(all: Iterable<TaskData>, value: Boolean = false) {
			for (data in all) {
				data.visited = value
			}
		}
	}
}
