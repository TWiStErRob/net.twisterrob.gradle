package net.twisterrob.gradle.graph.vis;

import java.util.Map;

import org.gradle.api.*;
import org.gradle.api.initialization.Settings;

import net.twisterrob.gradle.graph.tasks.*;

public class LoggingTaskVisualizer implements TaskVisualizer {
	private final TaskVisualizer wrapped;

	LoggingTaskVisualizer(TaskVisualizer wrapped) {
		this.wrapped = wrapped;
	}

	@Override public void showUI(Settings project) {
		System.out.printf("showUI(%s)\n", project);
		wrapped.showUI(project);
	}
	@Override public void initModel(Map<Task, TaskData> graph) {
		System.out.printf("initModel(%s)\n", graph);
		wrapped.initModel(graph);
	}
	@Override public void update(Task task, TaskResult result) {
		System.out.printf("update(%s, %s)\n", task, result);
		wrapped.update(task, result);
	}
	@Override public void closeUI() {
		System.out.printf("closeUI()\n");
		wrapped.closeUI();
	}
}
