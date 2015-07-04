package net.twisterrob.gradle.graph

import groovy.transform.CompileStatic
import net.twisterrob.gradle.graph.graphstream.GraphStreamTaskVisualizer
import net.twisterrob.gradle.graph.javafx.JavaFXD3TaskVisualizer
import org.gradle.*
import org.gradle.api.*
import org.gradle.api.execution.*
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.execution.*

@CompileStatic
public class GraphPlugin implements Plugin<Project> {
	private TaskVisualizer graph
	private Project project

	@Override public void apply(Project project) {
		this.project = project;
		project.afterEvaluate {
			project.tasks.all { Task task ->
				task.doLast {
					sleep((int)(Math.random() * 1000))
				}
			}
		}
		if (hasJavaFX()) {
			graph = new JavaFXD3TaskVisualizer()
		} else {
			graph = new GraphStreamTaskVisualizer()
		}
		graph.initModel(project)
		buildGraph(project)
		graph.showUI()

		project.gradle.buildFinished {
			graph.closeUI()
		}
	}

	boolean hasJavaFX() {
		try {
			javafx.application.Platform
			return true;
		} catch(NoClassDefFoundError ignore) {
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
	void buildGraph(Project project) {
		//decorateListeners(project)

		for (Task task : project.tasks) {
			graph.addTask(task)
		}

		project.tasks.whenTaskAdded { Task task ->
			graph.addTask(task)
		}

		project.gradle.taskGraph.addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
			@Override void graphPopulated(TaskExecutionGraph teg) {
				Collection<Task> requested = getRequestTasks()
				for (Task task : teg.allTasks) {
					def addState = requested.contains(task) ? EnumSet.of(TaskVisualizer.TaskState.requested) : EnumSet.noneOf(TaskVisualizer.TaskState)
					graph.setVisuals(task, addState, EnumSet.of(TaskVisualizer.TaskState.unknown))
					//println "${task.name}: ${task.dependsOn}"
					for (Object dep : task.dependsOn) {
						if (dep instanceof Task) {
							graph.addDependency(dep as Task, task)
						}
					}
				}
				Collection<Task> excluded = getExcludedTasks()
				for (Task task : excluded) {
					graph.setVisuals(task, EnumSet.of(TaskVisualizer.TaskState.excluded), EnumSet.of(TaskVisualizer.TaskState.unknown))
					for (Object dep : task.dependsOn) {
						if (dep instanceof Task) {
							graph.addDependency(dep as Task, task)
						}
					}
				}
			}

			/** @see ExcludedTaskFilteringBuildConfigurationAction */
			private Collection<Task> getExcludedTasks() {
				TaskSelector selector = ((GradleInternal)project.gradle).getServices().get(TaskSelector.class)

				Set<Task> tasks = new HashSet<>()
				for (String path : project.gradle.startParameter.excludedTaskNames) {
					TaskSelector.TaskSelection selection = selector.getSelection(path)
					//println "-${path} -> ${selection.getTasks()*.getName()}"
					tasks.addAll(selection.getTasks())
				}
				return tasks;
			}

			/** @see TaskNameResolvingBuildConfigurationAction */
			private Collection<Task> getRequestTasks() {
				TaskSelector selector = ((GradleInternal)project.gradle).getServices().get(TaskSelector.class)

				Set<Task> tasks = new HashSet<>()
				for (TaskExecutionRequest request : project.gradle.startParameter.taskRequests) {
					for (String path : request.args) {
						TaskSelector.TaskSelection selection = selector.getSelection(request.projectPath, path)
						//println "${request.projectPath}:${path} -> ${selection.getTasks()*.getName()}"
						tasks.addAll(selection.getTasks())
					}
				}
				return tasks;
			}
		})
		project.gradle.taskGraph.addTaskExecutionListener(new TaskExecutionListener() {
			@Override void beforeExecute(Task task) {
				graph.setVisuals(task, EnumSet.of(TaskVisualizer.TaskState.executing), EnumSet.noneOf(TaskVisualizer.TaskState))
			}
			@Override void afterExecute(Task task, TaskState state) {
				EnumSet<TaskVisualizer.TaskState> states = EnumSet.noneOf(TaskVisualizer.TaskState)
				if (!state.didWork) {
					states.add(TaskVisualizer.TaskState.result_nowork)
				}
				if (state.executed) {
					states.add(TaskVisualizer.TaskState.executed)
				}
				if (state.skipped && state.skipMessage == "SKIPPED") {
					states.add(TaskVisualizer.TaskState.result_skipped)
				}
				if (state.skipped && state.skipMessage == "UP-TO-DATE") {
					states.add(TaskVisualizer.TaskState.result_uptodate)
				}
				if (state.failure) {
					states.add(TaskVisualizer.TaskState.result_failure)
				}
				println "$task.name: ${states}"
				graph.setVisuals(task, states, EnumSet.of(TaskVisualizer.TaskState.executing))
			}
		})
	}

	private void decorateListeners(Project project) {
		project.gradle.buildStarted { x ->
			println "buildStarted $x"
		}
		project.gradle.beforeProject { proj ->
			println "beforeProject $proj"
		}
		project.gradle.afterProject { proj ->
			println "afterProject $proj"
		}
		project.gradle.buildFinished { x ->
			println "buildFinished $x"
		}
		project.gradle.addProjectEvaluationListener(new ProjectEvaluationListener() {
			@Override void beforeEvaluate(Project proj) {
				println "beforeEvaluate $proj"
			}
			@Override void afterEvaluate(Project proj, ProjectState state) {
				println "afterEvaluate $proj"
			}
		})
		project.gradle.addBuildListener(new BuildListener() {
			@Override void buildStarted(Gradle gradle) {
				println "buildStarted $gradle"
			}
			@Override void settingsEvaluated(Settings settings) {
				println "settingsEvaluated $settings"
			}
			@Override void projectsLoaded(Gradle gradle) {
				println "projectsLoaded $gradle"
			}
			@Override void projectsEvaluated(Gradle gradle) {
				println "projectsEvaluated $gradle"
			}
			@Override void buildFinished(BuildResult result) {
				println "buildFinished $result"
			}
		})

		project.afterEvaluate {
			println "afterEvaluate"
		}
		project.beforeEvaluate {
			println "beforeEvaluate"
		}
		project.tasks.whenTaskAdded { task ->
			println "whenTaskAdded $task"
		}

		def teg = project.gradle.taskGraph
		teg.afterTask {
			println "afterTask $it"
		}
		teg.beforeTask {
			println "beforeTask $it"
		}
		teg.addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
			@Override void graphPopulated(TaskExecutionGraph graph) {
				println "graphPopulated $graph"
				assert teg == graph
			}
		})
		teg.addTaskExecutionListener(new TaskExecutionListener() {
			@Override void beforeExecute(Task task) {
				println "beforeExecute $task"
			}
			@Override void afterExecute(Task task, TaskState state) {
				println "afterExecute $task"
			}
		})
		teg.whenReady {
			println "whenReady"
		}
	}
}
