package net.twisterrob.gradle.graph.graphstream;

import java.io.IOException;
import java.util.*;

import javax.swing.*;

import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.gradle.api.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.view.Viewer;

import net.twisterrob.gradle.graph.*;

import static net.twisterrob.gradle.graph.graphstream.GraphExtensions.*;

public class GraphStreamTaskVisualizer implements TaskVisualizer {
	private Graph graph;
	private Viewer viewer;
	private Layout layout;

	@Override public void showUI(Project project) {
		graph = new SingleGraph(project.getName());
		try {
			String css = IOGroovyMethods.getText(getClass().getResourceAsStream("/graphstream.css"));
			graph.addAttribute("ui.stylesheet", css);
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read style sheet");
		}
		graph.setStrict(true);
		graph.setAutoCreate(false);

		layout = new LinLog(false);
		viewer = graph.display();
		JFrame window = (JFrame)SwingUtilities.getWindowAncestor(viewer.getDefaultView());
		window.createBufferStrategy(1);
		window.setLocation(-1375, 1265);
		window.setSize(1384, 786);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);
		//viewer.enableAutoLayout(layout);
	}

	private Node createNode(TaskData data) {
		Node node = graph.addNode(id(data.getTask()));
		setLabel(node, data.getTask().getName());
		addClass(node, classMappingType.get(data.getType()));
		return node;
	}

	@Override public void initModel(Map<Task, TaskData> tasks) {
		for (TaskData data : tasks.values()) {
			createNode(data);
		}
		for (TaskData data : tasks.values()) {
			Node from = graph.getNode(id(data.getTask()));
			for (TaskData dep : data.getDepsDirect()) {
				Node to = graph.getNode(id(dep.getTask()));
				graph.addEdge(id(data.getTask(), dep.getTask()), from, to, true);
			}
		}
	}

	public void closeUI() {
		//println window.getLocation()
		//println window.getSize()
		viewer.removeView(Viewer.DEFAULT_VIEW_ID);
		viewer.close();
	}

	@Override public void update(Task task, TaskResult result) {
		Node node = graph.getNode(id(task));
		for (Map.Entry<TaskResult, String> possibleResult : classMappingResult.entrySet()) {
			removeClass(node, possibleResult.getValue());
		}
		String[] classes = addClass(node, classMappingResult.get(result));
		//System.out.println(task.getName() + ": " + Arrays.toString(classes));
	}

	private static final EnumMap<TaskResult, String> classMappingResult = new EnumMap<>(TaskResult.class);
	private static final EnumMap<TaskType, String> classMappingType = new EnumMap<>(TaskType.class);

	static {
		classMappingResult.put(TaskResult.executing, "executing");
		classMappingResult.put(TaskResult.completed, "executed");
		classMappingResult.put(TaskResult.nowork, "nowork");
		classMappingResult.put(TaskResult.skipped, "skipped");
		classMappingResult.put(TaskResult.uptodate, "uptodate");
		classMappingResult.put(TaskResult.failure, "failure");
		classMappingType.put(TaskType.unknown, "unknown");
		classMappingType.put(TaskType.normal, "norma");
		classMappingType.put(TaskType.requested, "requested");
		classMappingType.put(TaskType.excluded, "excluded");
	}

	private String id(Task task) {
		return task.getName();
	}

	private String id(Task from, Task to) {
		return id(from) + "->" + id(to);
	}
}
