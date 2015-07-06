package net.twisterrob.gradle.graph;

import java.util.Map;

import org.gradle.api.*;

public interface TaskVisualizer {
	void showUI(Project project);
	void initModel(Map<Task, TaskData> graph);
	void update(Task task, TaskResult result);
	void closeUI();
}
