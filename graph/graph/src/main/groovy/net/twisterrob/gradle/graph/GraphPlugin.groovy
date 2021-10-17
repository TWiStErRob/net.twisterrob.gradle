package net.twisterrob.gradle.graph

import groovy.transform.PackageScope
import net.twisterrob.gradle.graph.tasks.*
import net.twisterrob.gradle.graph.vis.TaskVisualizer
import net.twisterrob.gradle.graph.vis.d3.javafx.D3JavaFXTaskVisualizer
import net.twisterrob.gradle.graph.vis.graphstream.GraphStreamTaskVisualizer
import org.gradle.api.*
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.cache.*
import org.gradle.cache.internal.FileLockManager

import javax.inject.Inject

import static org.gradle.cache.internal.filelock.LockOptionsBuilder.*

// TODO @groovy.util.logging.Slf4j
class GraphPlugin implements Plugin<Project> {
	private final CacheRepository cacheRepository
	private Project project
	private TaskVisualizer vis
	private GraphSettingsExtension settings

	@Inject GraphPlugin(CacheRepository cacheRepository) {
		this.cacheRepository = cacheRepository
	}

	/** @see <a href="http://stackoverflow.com/a/11237184/253468">SO</a>                 */
	@Override void apply(Project project) {
		this.project = project;

		settings = project.extensions.create("graphSettings", GraphSettingsExtension.class) as GraphSettingsExtension

		def gatherer = new TaskGatherer(project)

		project.afterEvaluate {
			vis = createGraph()
			vis.showUI(project)
			gatherer.setSimplify(settings.simplifyGraph)
		}

		gatherer.setTaskGraphListener(new TaskGatherer.TaskGraphListener() {
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

		project.gradle.buildFinished {
			if (!settings.dontClose) {
				vis.closeUI()
			}
		}
	}

	private TaskVisualizer createGraph() {
		PersistentCache cache = cacheRepository
				.cache(project.gradle, "graphSettings")
				.withDisplayName("graph visualization settings")
				.withLockOptions(mode(FileLockManager.LockMode.None)) // Lock on demand
				.open()

		return settings.newVisualizer(cache)
	}

	@PackageScope static boolean hasJavaFX() {
		try {
			javafx.application.Platform
			return true;
		} catch (NoClassDefFoundError ignore) {
			def jvm = org.gradle.internal.jvm.Jvm.current()
			if (jvm.javaVersion.java7Compatible && new File("${jvm.jre.homeDir}/lib/jfxrt.jar").exists()) {
				def dependency = """
No JavaFX Runtime found on buildscript classpath, falling back to primitive GraphStream visualization
You can add JavaFX like this:
buildscript {
	dependencies {
		def jvm = ${org.gradle.internal.jvm.Jvm.name}.current()
		if (jvm.javaVersion.java7Compatible) {
			classpath files("\${jvm.jre.homeDir}/lib/jfxrt.jar")
		}
	}
}
or ask for GraphStream explicitly:
graphSettings {
	visualizer = ${GraphStreamTaskVisualizer.name}
}
				"""
				System.err.println(dependency.trim());
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

	static class GraphSettingsExtension {
		boolean dontClose = false
		/** a TaskVisualizer implementation class, null means automatic */
		Class<TaskVisualizer> visualizer = null
		boolean simplifyGraph = true

		@PackageScope TaskVisualizer newVisualizer(PersistentCache cache) {
			if (visualizer == null) {
				if (GraphPlugin.hasJavaFX()) {
					visualizer = D3JavaFXTaskVisualizer
				} else {
					visualizer = GraphStreamTaskVisualizer
				}
			}

			Throwable err = null;
			if (!TaskVisualizer.isAssignableFrom(visualizer)) {
				err = new IllegalArgumentException("visualizer must implement ${TaskVisualizer}").fillInStackTrace();
			}

			TaskVisualizer graph;
			if (cache != null) { // if we have a cache try cached ctor first
				try {
					graph = visualizer.newInstance([ cache ] as Object[])
				} catch (Exception ex) {
					err = ex
				}
			}
			if (graph == null) {
				try {
					graph = visualizer.newInstance()
				} catch (Exception ex) {
					err = ex
				}
			}
			if (graph == null) {
				throw new IllegalArgumentException("Invalid value for visualizer: ${visualizer}," +
						"make sure the class has a default or a ${PersistentCache} constructor", err);
			}
			return graph;
		}
	}
}
