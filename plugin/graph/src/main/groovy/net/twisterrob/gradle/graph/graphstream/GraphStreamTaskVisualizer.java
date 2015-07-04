package net.twisterrob.gradle.graph.graphstream;

import java.io.*;
import java.util.*;

import javax.swing.*;

import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.gradle.api.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.view.Viewer;

import net.twisterrob.gradle.graph.TaskVisualizer;

import static net.twisterrob.gradle.graph.graphstream.GraphExtensions.*;

public class GraphStreamTaskVisualizer implements TaskVisualizer {
	private Graph graph;
	private Viewer viewer;
	private Layout layout;

	public void initModel(Project project) {
		graph = new SingleGraph(project.getName());
		try {
			String css = IOGroovyMethods.getText(getClass().getResourceAsStream("/graphstream.css"));
			graph.addAttribute("ui.stylesheet", css);
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read style sheet");
		}
		graph.setStrict(false);
		graph.setAutoCreate(true);
	}

	@Override public void addTask(Task task) {
		//createNode(task);
	}
	private Node createNode(Task task) {
		Node node = graph.getNode(task.getName());
		if (node == null) {
			node = graph.addNode(task.getName());
			setLabel(node, task.getName());
			addClass(node, "unknown");
		}
		return node;
	}
	public void showUI() {
		layout = new LinLog(false);
		viewer = graph.display();
		JFrame window = (JFrame)SwingUtilities.getWindowAncestor(viewer.getDefaultView());
		window.createBufferStrategy(1);
		window.setLocation(-1375, 1265);
		window.setSize(1384, 786);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);
		//viewer.enableAutoLayout(layout);
	}

	public void closeUI() {
		//println window.getLocation()
		//println window.getSize()
		viewer.removeView(Viewer.DEFAULT_VIEW_ID);
		viewer.close();
	}

	private static final EnumMap<TaskState, String> classMapping = new EnumMap<TaskState, String>(TaskState.class);

	static {
		classMapping.put(TaskState.executing, "executing");
		classMapping.put(TaskState.result_nowork, "nowork");
		classMapping.put(TaskState.executed, "executed");
		classMapping.put(TaskState.result_skipped, "skipped");
		classMapping.put(TaskState.result_uptodate, "uptodate");
		classMapping.put(TaskState.result_failure, "failure");
		classMapping.put(TaskState.unknown, "unknown");
		classMapping.put(TaskState.requested, "requested");
		classMapping.put(TaskState.excluded, "excluded");
	}

	@Override public void setVisuals(Task task, EnumSet<TaskState> addStates, EnumSet<TaskState> removeStates) {
		Node node = createNode(task);
		for (TaskState remove : removeStates) {
			removeClass(node, classMapping.get(remove));
			removeClass(node.getEachLeavingEdge(), classMapping.get(remove));
		}
		for (TaskState add : addStates) {
			addClass(node, classMapping.get(add));
			addClass(node.getEachLeavingEdge(), classMapping.get(add));
		}
	}
	@Override public void addDependency(Task from, Task to) {
		createNode(from);
		createNode(to);
		graph.addEdge(from.getName() + "->" + to.getName(), from.getName(), to.getName(), true);
	}
}
