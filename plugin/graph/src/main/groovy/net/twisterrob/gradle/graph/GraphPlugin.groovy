package net.twisterrob.gradle.graph

import groovy.transform.CompileStatic
import net.twisterrob.gradle.graph.graphstream.GraphStreamTaskVisualizer
import net.twisterrob.gradle.graph.javafx.JavaFXTaskVisualizer
import org.gradle.api.*
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.cache.*
import org.gradle.cache.internal.FileLockManager

import javax.inject.Inject

import static org.gradle.cache.internal.filelock.LockOptionsBuilder.*

// TODO @groovy.util.logging.Slf4j
@CompileStatic
public class GraphPlugin implements Plugin<Project> {
	private final CacheRepository cacheRepository
	private Project project

	@Inject public GraphPlugin(CacheRepository cacheRepository) {
		this.cacheRepository = cacheRepository
	}

	/** @see <a href="http://stackoverflow.com/a/11237184/253468">SO</a> */
	@Override public void apply(Project project) {
		this.project = project;
		PersistentCache cache = cacheRepository
				.cache(project.gradle, "graphSettings")
				.withDisplayName("graph visualization settings")
				.withLockOptions(mode(FileLockManager.LockMode.None)) // Lock on demand
				.open()

		TaskVisualizer vis = createGraph(cache)

		new TaskGatherer(project).setTaskGraphListener(new TaskGatherer.TaskGraphListener() {
			@Override void graphPopulated(Map<Task, TaskData> tasks) {
				vis.initModel(tasks)
			}
		})

		project.gradle.taskGraph.addTaskExecutionListener(new TaskExecutionListener() {
			@Override void beforeExecute(Task task) {
				vis.update(task, TaskResult.executing)
			}
			@Override void afterExecute(Task task, TaskState state) {
				vis.update(task, getResult(task, state))
			}
		})

		vis.showUI(project)
		project.gradle.buildFinished {
			vis.closeUI()
			cache.close();
		}
	}

	private TaskVisualizer createGraph(PersistentCache cache) {
		TaskVisualizer graph
		if (hasJavaFX()) {
			graph = new JavaFXTaskVisualizer(cache)
		} else {
			graph = new GraphStreamTaskVisualizer(cache)
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

	private static TaskResult getResult(Task task, TaskState state) {
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
