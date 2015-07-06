package net.twisterrob.gradle.graph

import org.gradle.api.Task

class TaskData implements Comparable<TaskData> {
	TaskType type = TaskType.unknown;
	TaskResult state = null;
	final Task task;
	final List<TaskData> deps = new ArrayList<>();
	final List<TaskData> depsDirect = new ArrayList<>();
	final Map<TaskData, List<TaskData>> depsImplicit = new TreeMap<>();

	TaskData(Task task) {
		this.task = task;
	}

	@Override int compareTo(TaskData o) {
		return this.task.compareTo(o.task);
	}
}
