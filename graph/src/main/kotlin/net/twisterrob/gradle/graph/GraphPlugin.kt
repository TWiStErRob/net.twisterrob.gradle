package net.twisterrob.gradle.graph

import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskGatherer
import net.twisterrob.gradle.graph.tasks.TaskResult
import net.twisterrob.gradle.graph.vis.TaskVisualizer
import net.twisterrob.gradle.graph.vis.d3.javafx.D3JavaFXTaskVisualizer
import net.twisterrob.gradle.graph.vis.graphstream.GraphStreamTaskVisualizer
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.TaskState
import org.gradle.cache.CacheBuilder
import org.gradle.cache.FileLockManager
import org.gradle.cache.LockOptions
import org.gradle.cache.PersistentCache
import org.gradle.cache.scopes.ScopedCacheBuilderFactory
import org.gradle.util.GradleVersion
import javax.inject.Inject

private val LOG = logger<GraphPlugin>()

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class GraphPlugin @Inject constructor(
	private val cacheRepository: ScopedCacheBuilderFactory,
) : Plugin<Settings> {

	@Suppress("detekt.LateinitUsage") // TODO refactor to object.
	private lateinit var vis: TaskVisualizer

	/** See [SO](http://stackoverflow.com/a/11237184/253468). */
	override fun apply(settings: Settings) {
		val extension = settings.extensions.create("graphSettings", GraphSettingsExtension::class.java)

		val gatherer = TaskGatherer(settings)
		settings.gradle.addBuildListener(object : BuildAdapter() {
			override fun settingsEvaluated(settings: Settings) {
				vis = createGraph(extension.visualizer)
				vis.showUI(settings)
				gatherer.isSimplify = extension.isSimplifyGraph
			}

			@Deprecated("Not compatible with configuration cache.")
			override fun buildFinished(result: BuildResult) {
				if (!extension.isKeepOpen) {
					vis.closeUI()
				}
			}
		})

		gatherer.taskGraphListener = object : TaskGatherer.TaskGraphListener {
			override fun graphPopulated(graph: Map<Task, TaskData>) {
				vis.initModel(graph)
			}
		}

		@Suppress("DEPRECATION") // TODO Configuration cache.
		settings.gradle.taskGraph.addTaskExecutionListener(object : org.gradle.api.execution.TaskExecutionListener {
			override fun beforeExecute(task: Task) {
				vis.update(task, TaskResult.Executing)
			}

			override fun afterExecute(task: Task, state: TaskState) {
				vis.update(task, getResult(task, state))
			}
		})
	}

	private fun createGraph(visualizer: Class<out TaskVisualizer>?): TaskVisualizer {
		val cache = cacheRepository
			.createCacheBuilder("graphSettings")
			.withDisplayName("graph visualization settings")
			.withInitialLockModeCompat(FileLockManager.LockMode.None) // Lock on demand
			.open()

		return newVisualizer(visualizer, cache)
	}

	companion object {

		private fun getResult(task: Task, state: TaskState): TaskResult =
			when {
				state.failure != null ->
					TaskResult.Failure

				state.noSource ->
					TaskResult.NoSource

				// TaskExecutionOutcome.FROM_CACHE is also up-to-date, so this needs to be first.
				state.skipped && state.skipMessage == "FROM-CACHE" ->
					TaskResult.FromCache

				state.upToDate ->
					TaskResult.UpToDate

				state.skipped && state.skipMessage == "SKIPPED" ->
					TaskResult.Skipped

				!state.didWork ->
					TaskResult.NoWork

				state.executed && state.didWork ->
					TaskResult.Completed

				else ->
					error(
						"""
							What happened with ${task.name}? The task state is unrecognized:
								Executed: ${state.executed}
								Did work: ${state.didWork}
								Skipped: ${state.skipped}
								Skip message: ${state.skipMessage ?: "null"}
								Failure: ${state.failure ?: "null"}
						""".trimIndent()
					)
			}
	}
}

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class GraphSettingsExtension {

	var isKeepOpen: Boolean = false

	/** A [TaskVisualizer] implementation class, null means automatic. */
	var visualizer: Class<out TaskVisualizer>? = null

	var isSimplifyGraph: Boolean = true
}

private fun hasJavaFX(): Boolean =
	try {
		javafx.application.Platform::class.java
		true
	} catch (ex: NoClassDefFoundError) {
		val dependency = """
			No JavaFX Runtime found on buildscript classpath,
			falling back to primitive GraphStream visualization.
			You can ensure JavaFX or ask for GraphStream explicitly:
			graphSettings {
				visualizer = ${GraphStreamTaskVisualizer::class.java.name}
			}
		""".trimIndent()
		LOG.warn(dependency, ex)
		false
	}

private fun newVisualizer(visualizerClass: Class<out TaskVisualizer>?, cache: PersistentCache): TaskVisualizer {
	val visualizer = visualizerClass ?: if (hasJavaFX()) {
		D3JavaFXTaskVisualizer::class.java
	} else {
		GraphStreamTaskVisualizer::class.java
	}

	var err: Throwable? = null
	if (!TaskVisualizer::class.java.isAssignableFrom(visualizer)) {
		err = IllegalArgumentException("visualizer must implement ${TaskVisualizer::class.java}").fillInStackTrace()
	}

	var graph: TaskVisualizer? = null
	try {
		graph = visualizer.getConstructor(PersistentCache::class.java).newInstance(cache)
	} catch (ex: ReflectiveOperationException) {
		err = ex
	}
	if (graph == null) {
		try {
			graph = visualizer.getConstructor().newInstance()
		} catch (ex: ReflectiveOperationException) {
			err = ex
		}
	}
	if (graph == null) {
		throw IllegalArgumentException(
			"Invalid value for visualizer: ${visualizer}," +
					"make sure the class has a default or a ${PersistentCache::class.java} constructor",
			err
		)
	}
	return graph
}

private fun CacheBuilder.withInitialLockModeCompat(mode: FileLockManager.LockMode): CacheBuilder =
	run {
		if (GradleVersion.version("8.7") <= GradleVersion.current().baseVersion) {
			withInitialLockMode(mode)
		} else {
			//@formatter:off
			@Suppress("detekt.DataClassContainsFunctions") // Gradle API compatibility, needs hashCode/equals.
			data class SimpleLockOptions(private val mode: FileLockManager.LockMode) : LockOptions {
				override fun getMode(): FileLockManager.LockMode = this.mode
				override fun isUseCrossVersionImplementation(): Boolean = false
				override fun copyWithMode(mode: FileLockManager.LockMode): LockOptions = SimpleLockOptions(mode)
				@Override @Suppress("unused") // Hopefully "overrides" the Gradle <8.7 method.
				fun withMode(mode: FileLockManager.LockMode): LockOptions = SimpleLockOptions(mode)
			}
			//@formatter:on
			val withLockOptions = CacheBuilder::class.java.getMethod("withLockOptions", LockOptions::class.java)
			withLockOptions.invoke(this, SimpleLockOptions(mode)) as CacheBuilder
		}
	}
