package net.twisterrob.gradle.graph

import groovy.transform.CompileStatic
import org.gradle.TaskExecutionRequest
import org.gradle.api.*
import org.gradle.api.execution.*
import org.gradle.api.internal.GradleInternal
import org.gradle.execution.TaskSelector

@CompileStatic
public class TaskGatherer implements TaskExecutionGraphListener {
	private TaskGraphListener listener
	interface TaskGraphListener {
		void graphPopulated(Map<Task, TaskData> graph)
	}

	private final Map<Task, TaskData> all = new TreeMap<>();
	@SuppressWarnings("GrFinalVariableAccess")
	private final Project project

	TaskGatherer(Project project) {
		this.project = project

		// existing tasks (in case plugin is applied late)
		for (Task task : project.tasks) {
			data(task)
		}

		// future tasks
		project.tasks.whenTaskAdded { Task task ->
			data(task)
		}

		// final tasks
		project.gradle.taskGraph.addTaskExecutionGraphListener(this)
	}

	void setTaskGraphListener(TaskGraphListener listener) {
		this.listener = listener
	}

	@Override void graphPopulated(TaskExecutionGraph teg) {
		for (Task task : teg.allTasks) {
			data(task).type = TaskType.normal
		}
		for (Task task : getRequestTasks()) {
			data(task).type = TaskType.requested
		}
		for (Task task : getExcludedTasks()) {
			data(task).type = TaskType.excluded
		}
		buildDependencies()
		calculateSimplification()
		if (listener != null) {
			listener.graphPopulated(all)
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

	private void buildDependencies() {
		for (TaskData taskData : all.values()) {
			for (Object dep : taskData.task.dependsOn) {
				if (dep instanceof Task) {
					taskData.deps.add(data((Task)dep));
				}
			}
		}
	}

	private void calculateSimplification() {
		new TransitiveReduction().run(all.values());
	}

	/** @see org.gradle.execution.ExcludedTaskFilteringBuildConfigurationAction */
	private Collection<Task> getExcludedTasks() {
		TaskSelector selector = ((GradleInternal)project.gradle).getServices().get(TaskSelector.class)

		Set<Task> tasks = new HashSet<>()
		for (String path : project.gradle.startParameter.excludedTaskNames) {
			TaskSelector.TaskSelection selection = selector.getSelection(path)
			//println "-${path} -> ${selection.getTasks()*.getName()}"
			tasks.addAll(selection.getTasks())
		}
		return tasks;
	}

	/** @see org.gradle.execution.TaskNameResolvingBuildConfigurationAction */
	private Collection<Task> getRequestTasks() {
		TaskSelector selector = ((GradleInternal)project.gradle).getServices().get(TaskSelector.class)

		Set<Task> tasks = new HashSet<>()
		for (TaskExecutionRequest request : project.gradle.startParameter.taskRequests) {
			for (String path : request.args) {
				TaskSelector.TaskSelection selection = selector.getSelection(request.projectPath, path)
				//println "${request.projectPath}:${path} -> ${selection.getTasks()*.getName()}"
				tasks.addAll(selection.getTasks())
			}
		}
		return tasks;
	}

	/** @see <a href="http://stackoverflow.com/a/11237184/253468">SO</a>     */
	private static class TransitiveReduction {
		/** nodes that are done */
		private final Set<TaskData> done = new TreeSet<>()
		/** list of nodes to get from vertex0 to child0 */
		private final Deque<TaskData> path = new LinkedList<>()

		public void run(Collection<TaskData> graph) {
			for (TaskData vertex0 in graph) {
				vertex0.depsDirect.addAll(vertex0.deps);
			}
			for (TaskData vertex0 in graph) { // for vertex0 in vertices
				depthFirstSearch(vertex0, vertex0);
			}
		}

		/**
		 * Runs a DFS on graph starting from vertex0
		 * @param vertex0 root of DFS search
		 * @param child0 current node during search
		 */
		void depthFirstSearch(TaskData vertex0, TaskData child0) {
			if (done.contains(child0)) { // if child0 in done
				return
			}

			path.push(child0)
			for (TaskData child in child0.depsDirect) { // for child in child0.children
				if (child0 != vertex0) {
					if (vertex0.depsDirect.remove(child)) { // edges.discard((vertex0, child))
						List<TaskData> alternatePath = new ArrayList<>(path)
						alternatePath.add(child)
						println "${vertex0.task.name}->${child.task.name}" +
								"is replaced by ${alternatePath*.task*.name.join('->')}"
						def old = vertex0.depsImplicit.put(child, alternatePath)
						if (old != null) {
							println "Path already existed: ${old*.task*.name.join('->')}"
						}
					}
				}
				depthFirstSearch(vertex0, child) // depthFirstSearch(edges, vertex0, child, done)
			}
			path.pop()
			done.add(child0)
		}
	}
}
