package net.twisterrob.gradle.graph.tasks

import org.gradle.TaskExecutionRequest
import org.gradle.api.*
import org.gradle.api.execution.*
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.tasks.TaskDependencyResolveException
import org.gradle.execution.TaskSelector

class TaskGatherer implements TaskExecutionGraphListener {
	TaskGraphListener taskGraphListener
	boolean simplify;

	interface TaskGraphListener {
		void graphPopulated(Map<Task, TaskData> graph)
	}

	private final Map<Task, TaskData> all = new TreeMap<>();
	private final Project project

	TaskGatherer(Project project) {
		this.project = project
		wire()
	}

	private void wire() {
		// existing tasks (in case plugin is applied late)
		for (Task task in project.tasks) {
			data(task)
		}

		// future tasks
		project.tasks.whenTaskAdded { Task task ->
			data(task)
		}

		// final tasks
		project.gradle.taskGraph.addTaskExecutionGraphListener(this)
	}

	@Override void graphPopulated(TaskExecutionGraph teg) {
		for (Task task in teg.allTasks) {
			data(task).type = TaskType.normal
		}
		for (Task task in requestedTasks) {
			data(task).type = TaskType.requested
		}
		for (Task task in excludedTasks) {
			data(task).type = TaskType.excluded // wins over requested
		}
		new ResolveDependencies(this.&data).run(new ArrayList(all.values()))
		if (simplify) {
			new TransitiveReduction().run(all.values())
		}
		if (taskGraphListener != null) {
			taskGraphListener.graphPopulated(all)
		}
	}

	public TaskData data(Task task) {
		TaskData data = all[task];
		if (data == null) {
			data = new TaskData(task);
			all[task] = data;
		}
		return data;
	}

	// Last existed in https://github.com/gradle/gradle/blob/v7.1.0/subprojects/core/src/main/java/org/gradle/execution/ExcludedTaskFilteringBuildConfigurationAction.java
	/** @see org.gradle.execution.ExcludedTaskFilteringBuildConfigurationAction */
	private Collection<Task> getExcludedTasks() {
		TaskSelector selector = ((GradleInternal)project.gradle).getServices().get(TaskSelector.class)

		Set<Task> tasks = new HashSet<>()
		for (String path in project.gradle.startParameter.excludedTaskNames) {
			//TaskSelector.TaskSelection selection = selector.getSelection(path)
			//println "-${path} -> ${selection.getTasks()*.getName()}"
			//tasks.addAll(selection.filter)
		}
		return tasks;
	}

	// Last existed in https://github.com/gradle/gradle/blob/v7.5.0/subprojects/core/src/main/java/org/gradle/execution/TaskNameResolvingBuildConfigurationAction.java
	/** @see org.gradle.execution.TaskNameResolvingBuildConfigurationAction */
	private Collection<Task> getRequestedTasks() {
		TaskSelector selector = ((GradleInternal)project.gradle).getServices().get(TaskSelector.class)

		Set<Task> tasks = new HashSet<>()
		for (TaskExecutionRequest request in project.gradle.startParameter.taskRequests) {
			for (String path in request.args) {
				//TaskSelector.TaskSelection selection = selector.getSelection(request.projectPath, path)
				//println "${request.projectPath}:${path} -> ${selection.getTasks()*.getName()}"
				//tasks.addAll selection.tasks
			}
		}
		return tasks;
	}

	private static class ResolveDependencies {
		Closure<TaskData> dataForTask;

		ResolveDependencies(Closure<TaskData> dataForTask) {
			this.dataForTask = dataForTask;
		}

		void run(Collection<TaskData> graph) {
			TaskData.resetVisited graph
			for (TaskData taskData in graph) {
				addResolvedDependencies taskData
			}
		}

		private void addResolvedDependencies(TaskData taskData) {
			if (taskData.visited) {
				return // shortcut, because taskDependencies.getDependencies is really expensive
			}
			Set<Task> deps
			try {
				deps = taskData.task.taskDependencies.getDependencies(taskData.task) as Set<Task>
			} catch (TaskDependencyResolveException ignore) {
				// STOPSHIP why is this erroring?
				println(ignore)
				deps = []
			}
			for (Task dep in deps) {
				def data = dataForTask(dep)
				taskData.deps.add data
				addResolvedDependencies data
			}
			taskData.visited = true
		}
	}

	/** @see <a href="http://stackoverflow.com/a/11237184/253468">SO</a>           */
	private static class TransitiveReduction {
		/** list of nodes to get from vertex0 to child0 */
		private final List<TaskData> path = new ArrayList<>(10)

		void run(Collection<TaskData> graph) {
			for (TaskData vertex0 in graph) {
				vertex0.depsDirect.addAll(vertex0.deps);
			}
			for (TaskData vertex0 in graph) { // for vertex0 in vertices
				TaskData.resetVisited graph
				path.clear()
				depthFirstSearch vertex0, vertex0
			}
		}

		/**
		 * Runs a DFS on graph starting from vertex0
		 * @param vertex0 root of DFS search
		 * @param child0 current node during search
		 */
		private void depthFirstSearch(TaskData vertex0, TaskData child0) {
			if (child0.visited) {
				return
			}

			path.add child0
			for (TaskData child in new ArrayList<TaskData>(child0.depsDirect)) { // for child in child0.children
				if (vertex0 != child0) {
					tryReduce vertex0, child
				} else {
					// if vertex0 == child0 then child is surely a direct dependency of vertex0
					// in that case take no action to avoid simplifying a->b to a->b
				}
				depthFirstSearch vertex0, child // depthFirstSearch(edges, vertex0, child, done)
			}
			path.remove(path.size() - 1)

			child0.visited = true
		}

		private void tryReduce(TaskData vertex0, TaskData child) {
			if (vertex0.depsDirect.remove(child)) { // edges.discard((vertex0, child))
				def alternatePath = path + child as List;
				//println "${vertex0.task.name} -> ${child.task.name}" +
				//		" is replaced by ${alternatePath*.task*.name.join(' -> ')}"
				def old = vertex0.depsImplicit.put(child, alternatePath)
				if (old != null) {
					//println "Path already existed: ${old*.task*.name.join(' -> ')}"
				}
			}
		}
	}
}
