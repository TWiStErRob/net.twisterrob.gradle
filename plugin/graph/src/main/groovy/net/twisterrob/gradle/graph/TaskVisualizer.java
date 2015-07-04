package net.twisterrob.gradle.graph;

import java.util.EnumSet;

import org.gradle.api.*;

public interface TaskVisualizer {
	void addTask(Task task);
	void addDependency(Task from, Task to);
	void showUI();
	void initModel(Project project);
	void closeUI();
	void setVisuals(Task task, EnumSet<TaskState> addStates, EnumSet<TaskState> removeStates);

	enum TaskState {
		unknown,
		requested,
		excluded,
		executing,
		executed,
		result_nowork,
		result_skipped,
		result_uptodate,
		result_failure
	}
}
