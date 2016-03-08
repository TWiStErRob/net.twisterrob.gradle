package net.twisterrob.gradle.graph.vis;

import java.util.Map;

import org.gradle.api.*;

import net.twisterrob.gradle.graph.tasks.*;

public interface TaskVisualizer {
	void showUI(Project project);
	void initModel(Map<Task, TaskData> graph);
	void update(Task task, TaskResult result);
	/**
	 * Responsible for
	 *
	 * Closing the UI:
	 * <li>hiding if the UI is reusable
	 * <li>disposing of it if it needs to be recreated next time
	 *
	 * The cache received in the constructor should be also closed, always, even if the UI failed.
	 */
	void closeUI();
}
