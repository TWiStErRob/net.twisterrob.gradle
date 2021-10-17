package net.twisterrob.gradle.graph.vis;

import java.util.Map;

import org.gradle.api.*;

import net.twisterrob.gradle.graph.tasks.*;

public class DummyTaskVisualizer implements TaskVisualizer {
	@Override public void showUI(Project project) {
		// do nothing
	}
	@Override public void initModel(Map<Task, TaskData> graph) {
		// do nothing
	}
	@Override public void update(Task task, TaskResult result) {
		// do nothing
	}
	@Override public void closeUI() {
		// do nothing
	}
}
