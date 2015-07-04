package net.twisterrob.gradle.graph.javafx;

import java.util.EnumSet;

import org.gradle.api.*;

import net.twisterrob.gradle.graph.TaskVisualizer;

// http://stackoverflow.com/a/20125944/253468
public class JavaFXD3TaskVisualizer implements TaskVisualizer {
	Project project;
	GraphWindow window;

	public void initModel(Project project) {
		this.project = project;
		HelloJavaFX2.startLaunch();
	}

	public void showUI() {
		window = HelloJavaFX2.show(project);
	}

	public void closeUI() {
		//HelloJavaFX2.hide();
	}

	@Override public void addTask(Task task) {
		//window.addTask(task);
	}

	@Override public void setVisuals(Task task, EnumSet<TaskState> addStates, EnumSet<TaskState> removeStates) {
		window.setClasses(task, addStates, removeStates);
	}
	@Override public void addDependency(Task from, Task to) {
		window.addDependency(from, to);
	}
}
