package net.twisterrob.gradle.graph.tasks

import net.twisterrob.gradle.graph.Debug
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.tasks.TaskDependencyResolveException
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.execution.TaskSelector
import java.util.TreeMap

class TaskGatherer(
	private val settings: Settings
) {

	var taskGraphListener: TaskGraphListener? = null
	var isSimplify: Boolean = false

	interface TaskGraphListener {

		fun graphPopulated(graph: Map<Task, TaskData>)
	}

	private val all: MutableMap<Task, TaskData> = TreeMap()

	init {
		// https://github.com/gradle/gradle/issues/25340
		settings.gradle.taskGraph.addTaskExecutionGraphListener { teg ->
			for (task in teg.allTasks) {
				data(task).type = TaskType.Normal
			}
			for (task in getRequestedTasks()) {
				data(task).type = TaskType.Requested
			}
			for (task in getExcludedTasks()) {
				data(task).type = TaskType.Excluded // wins over requested
			}
			ResolveDependencies(::data).run(all.values.toList())
			if (isSimplify) {
				TransitiveReduction().run(all.values)
			}
			if (Debug.Gradle) {
				@Suppress("ForbiddenMethodCall") // TODO logging
				println("graphPopulated")
			}
			taskGraphListener?.run { graphPopulated(all) }
		}
		settings.gradle.addProjectEvaluationListener(object : ProjectEvaluationListener {
			override fun beforeEvaluate(project: Project) {
				project.tasks.configureEach { task ->
					if (Debug.Gradle) {
						@Suppress("ForbiddenMethodCall") // TODO logging
						println("configureEach: $task")
					}
					data(task)
				}
			}

			override fun afterEvaluate(project: Project, state: ProjectState) {
				// Nothing to do, but mandatory override.
			}
		})
	}

	private fun data(task: Task): TaskData {
		var data = all[task]
		if (data == null) {
			data = TaskData(task)
			all[task] = data
		}
		return data
	}

	// Last existed in https://github.com/gradle/gradle/blob/v7.1.0/subprojects/core/src/main/java/org/gradle/execution/ExcludedTaskFilteringBuildConfigurationAction.java
	/** @see org.gradle.execution.ExcludedTaskFilteringBuildConfigurationAction */
	private fun getExcludedTasks(): Iterable<Task> {
		@Suppress("UNUSED_VARIABLE") // TODO figure out how to reinstate this
		val selector = (settings.gradle as GradleInternal).serviceOf<TaskSelector>()
		val tasks: MutableSet<Task> = HashSet()
		for (path in settings.gradle.startParameter.excludedTaskNames) {
//			val selection = selector.getSelection(path)
//			println("-${path} -> ${selection.tasks.map { it.name }}")
//			tasks.addAll(selection.tasks)
		}
		return tasks
	}

	// Last existed in https://github.com/gradle/gradle/blob/v7.5.0/subprojects/core/src/main/java/org/gradle/execution/TaskNameResolvingBuildConfigurationAction.java
	/** @see org.gradle.execution.TaskNameResolvingBuildConfigurationAction */
	private fun getRequestedTasks(): Iterable<Task> {
		@Suppress("UNUSED_VARIABLE") // TODO figure out how to reinstate this
		val selector = (settings.gradle as GradleInternal).serviceOf<TaskSelector>()
		val tasks: MutableSet<Task> = HashSet()
		for (request in settings.gradle.startParameter.taskRequests) {
			for (path in request.args) {
//				val selection = selector.getSelection(request.projectPath, path)
//				println("${request.projectPath}:${path} -> ${selection.tasks.map { it.name }}")
//				tasks.addAll(selection.tasks)
			}
		}
		return tasks
	}
}

private class ResolveDependencies(
	private val dataForTask: (Task) -> TaskData
) {

	fun run(graph: Collection<TaskData>) {
		TaskData.resetVisited(graph)
		for (taskData in graph) {
			addResolvedDependencies(taskData)
		}
	}

	private fun addResolvedDependencies(taskData: TaskData) {
		if (taskData.isVisited) {
			return // shortcut, because taskDependencies.getDependencies is really expensive
		}
		val deps: Set<Task> =
			try {
				// TODO why is this erroring?
				taskData.task.taskDependencies.getDependencies(taskData.task) as Set<Task>
			} catch (ignore: TaskDependencyResolveException) {
				@Suppress("ForbiddenMethodCall") // TODO logging
				println(ignore)
				emptySet()
			}
		for (dep in deps) {
			val data = dataForTask(dep)
			taskData.deps.add(data)
			addResolvedDependencies(data)
		}
		taskData.isVisited = true
	}
}

/** @see <a href="http://stackoverflow.com/a/11237184/253468">SO</a> */
private class TransitiveReduction {

	/** list of nodes to get from `vertex0` to `child0`. */
	private val path: MutableList<TaskData> = ArrayList(@Suppress("MagicNumber") 10)

	fun run(graph: Collection<TaskData>) {
		for (vertex0 in graph) {
			vertex0.depsDirect.addAll(vertex0.deps)
		}
		for (vertex0 in graph) { // for vertex0 in vertices
			TaskData.resetVisited(graph)
			path.clear()
			depthFirstSearch(vertex0, vertex0)
		}
	}

	/**
	 * Runs a DFS on graph starting from vertex0.
	 *
	 * @param vertex0 root of DFS search
	 * @param child0 current node during search
	 */
	private fun depthFirstSearch(vertex0: TaskData, child0: TaskData) {
		if (child0.isVisited) {
			return
		}

		path.add(child0)
		for (child in ArrayList<TaskData>(child0.depsDirect)) { // for child in child0.children
			if (vertex0 != child0) {
				tryReduce(vertex0, child)
			} else {
				// if vertex0 == child0 then child is surely a direct dependency of vertex0
				// in that case take no action to avoid simplifying a->b to a->b
			}
			depthFirstSearch(vertex0, child) // depthFirstSearch(edges, vertex0, child, done)
		}
		path.removeAt(path.size - 1)

		child0.isVisited = true
	}

	private fun tryReduce(vertex0: TaskData, child: TaskData) {
		if (vertex0.depsDirect.remove(child)) { // edges.discard((vertex0, child))
			val alternatePath = path + child
			//println "${vertex0.task.name} -> ${child.task.name}" +
			//		" is replaced by ${alternatePath*.task*.name.join(' -> ')}"
			val old = vertex0.depsImplicit.put(child, alternatePath)
			if (old != null) {
				//println "Path already existed: ${old*.task*.name.join(' -> ')}"
			}
		}
	}
}
