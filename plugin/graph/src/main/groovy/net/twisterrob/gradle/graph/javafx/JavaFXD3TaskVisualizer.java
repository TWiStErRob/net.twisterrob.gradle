package net.twisterrob.gradle.graph.javafx;

import java.util.Map;

import org.gradle.api.*;

import net.twisterrob.gradle.graph.*;

// http://stackoverflow.com/a/20125944/253468
public class JavaFXD3TaskVisualizer implements TaskVisualizer {
	GraphWindow window;

	@Override public void showUI(Project project) {
		HelloJavaFX2.startLaunch();
		window = HelloJavaFX2.show(project);
	}

	@Override public void initModel(Map<Task, TaskData> graph) {
		window.initModel(graph);
	}

	@Override public void update(Task task, TaskResult result) {
		window.update(task, result);
	}

	@Override public void closeUI() {
		HelloJavaFX2.hide();
	}
}
