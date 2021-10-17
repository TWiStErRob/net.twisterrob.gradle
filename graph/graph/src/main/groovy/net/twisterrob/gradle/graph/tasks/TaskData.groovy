package net.twisterrob.gradle.graph.tasks

import org.gradle.api.Task

class TaskData implements Comparable<TaskData> {
	TaskType type = TaskType.unknown;
	TaskResult state = null;
	final Task task;
	final Set<TaskData> deps = new TreeSet<>();
	final Set<TaskData> depsDirect = new TreeSet<>();
	final Map<TaskData, List<TaskData>> depsImplicit = new TreeMap<>();

	/**
	 * Optimization to prevent <code>Set&lt;TaskData&gt; done</code> in algorithms.
	 * It's simpler to set a boolean flag than to construct complex data structures, though not as nice.
	 *
	 * @see #resetVisited
	 */
	boolean visited;

	TaskData(Task task) {
		this.task = task;
	}

	@Override int compareTo(TaskData o) {
		return this.task.compareTo(o.task);
	}

	@Override String toString() {
		return String.format("{%s, type=%s, state=%s, deps=%s}", task.path, type, state, deps*.task*.path);
	}

	/**
	 * @see #visited
	 */
	static void resetVisited(Iterable<TaskData> all, boolean value = false) {
		for(TaskData data in all) {
			data.visited = value
		}
	}
}
