package net.twisterrob.gradle.graph.vis.d3.javafx

import javafx.stage.Window
import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import net.twisterrob.gradle.graph.vis.TaskVisualizer
import org.gradle.api.Task
import org.gradle.cache.PersistentCache

/**
 * See [Idea from SO](http://stackoverflow.com/a/20125944/253468).
 */
class D3JavaFXTaskVisualizer(cache: PersistentCache) : TaskVisualizer {

	private var window: GraphWindow? = null
	private val settings: Settings

	init {
		settings = Settings(cache)
	}

	override fun showUI(project: org.gradle.api.initialization.Settings) {
		JavaFXApplication.startLaunch(settings.settings)
		window = JavaFXApplication.show(project)?.also { window ->
			settings.settings.applyTo(window.stage)
			window.stage.setOnHiding { event ->
				settings.settings = Settings.WindowLocation(event.source as Window)
				settings.close()
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
			settings.close()
		}
		window = null
		JavaFXApplication.hide()
	}
}
