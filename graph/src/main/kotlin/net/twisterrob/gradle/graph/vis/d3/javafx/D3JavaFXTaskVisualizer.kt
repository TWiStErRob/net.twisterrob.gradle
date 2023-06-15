package net.twisterrob.gradle.graph.vis.d3.javafx

import javafx.stage.Window
import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import net.twisterrob.gradle.graph.vis.TaskVisualizer
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.gradle.cache.PersistentCache

/**
 * See [Idea from SO](http://stackoverflow.com/a/20125944/253468).
 */
class D3JavaFXTaskVisualizer(cache: PersistentCache) : TaskVisualizer {

	private var window: GraphWindow? = null
	private val options: Options

	init {
		options = Options(cache)
	}

	override fun showUI(settings: Settings) {
		JavaFXApplication.startLaunch(options.options)
		window = JavaFXApplication.show(settings)?.also { window ->
			options.options.applyTo(window.stage)
			window.stage.setOnHiding { event ->
				options.options = Options.WindowLocation(event.source as Window)
				options.close()
			}
		}
	}

	override fun initModel(graph: Map<Task, TaskData>) {
		window?.initModel(graph)
	}

	override fun update(task: Task, result: TaskResult) {
		window?.update(task, result)
	}

	override fun closeUI() {
		if (window == null) {
			options.close()
		}
		window = null
		JavaFXApplication.hide()
	}
}
