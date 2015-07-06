package net.twisterrob.gradle.graph
import groovy.transform.CompileStatic
import net.twisterrob.gradle.graph.graphstream.GraphStreamTaskVisualizer
import net.twisterrob.gradle.graph.javafx.JavaFXD3TaskVisualizer
import org.gradle.api.*
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

@CompileStatic
public class GraphPlugin implements Plugin<Project> {
	private Project project

	/** @see <a href="http://stackoverflow.com/a/11237184/253468">SO</a>    */
	@Override public void apply(Project project) {
		this.project = project;
		project.afterEvaluate {
			project.tasks.all { Task task ->
				task.doLast {
					sleep((int)(Math.random() * 1000))
				}
			}
		}

		TaskVisualizer graph = createGraph()

		new TaskGatherer(project).setTaskGraphListener(new TaskGatherer.TaskGraphListener() {
			@Override void graphPopulated(Map<Task, TaskData> tasks) {
				graph.initModel(tasks)
			}
		})
		//decorateListeners(project)
		project.gradle.taskGraph.addTaskExecutionListener(new TaskExecutionListener() {
			@Override void beforeExecute(Task task) {
				graph.update(task, TaskResult.executing)
			}
			@Override void afterExecute(Task task, TaskState state) {
				graph.update(task, getResult(task, state))
			}
		})

		graph.showUI(project)
		project.gradle.buildFinished {
			graph.closeUI()
		}
	}
	private TaskVisualizer createGraph() {
		TaskVisualizer graph
		if (hasJavaFX()) {
			graph = new JavaFXD3TaskVisualizer()
		} else {
			graph = new GraphStreamTaskVisualizer()
		}
		return graph;
	}

	boolean hasJavaFX() {
		try {
			javafx.application.Platform
			return true;
		} catch (NoClassDefFoundError ignore) {
			def jvm = org.gradle.internal.jvm.Jvm.current()
			if (jvm.javaVersion.java7Compatible && new File("${jvm.jre.homeDir}/lib/jfxrt.jar").exists()) {
				def dependency = '''
No JavaFX Runtime found on buildscript classpath, falling back to primitive GraphStream visualization
You can add JavaFX like this:
buildscript {
	dependencies {
		def jvm = org.gradle.internal.jvm.Jvm.current()
		if (jvm.javaVersion.java7Compatible) {
			classpath files("${jvm.jre.homeDir}/lib/jfxrt.jar")
		}
	}
}
				'''
				project.logger.warn dependency.trim()
			}
			return false;
		}
	}

	private TaskResult getResult(Task task, TaskState state) {
		TaskResult result;
		if (state.failure) {
			result = TaskResult.failure
		} else if (state.skipped && state.skipMessage == "SKIPPED") {
			result = TaskResult.skipped
		} else if (state.skipped && state.skipMessage == "UP-TO-DATE") {
			result = TaskResult.uptodate
		} else if (!state.didWork) {
			result = TaskResult.nowork
		} else if (state.executed && state.didWork) {
			result = TaskResult.completed
		} else {
			throw new IllegalStateException("What happened with ${task.name}? The task state is unrecognized:\n"
					+ "\tExecuted: ${state.executed}\n"
					+ "\tDid work: ${state.didWork}\n"
					+ "\tSkipped: ${state.skipped}\n"
					+ "\tSkip message: ${state.skipMessage}\n"
					+ "\tFailure: ${state.failure}"
			)
		}
		return result;
	}
}
