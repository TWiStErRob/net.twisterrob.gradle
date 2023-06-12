package net.twisterrob.gradle.graph.vis.graphstream

import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import net.twisterrob.gradle.graph.tasks.TaskType
import net.twisterrob.gradle.graph.vis.TaskVisualizer
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.gradle.cache.PersistentCache
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.MultiGraph
import org.graphstream.ui.view.Viewer
import java.awt.Component
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import java.util.EnumMap
import javax.swing.SwingUtilities

class GraphStreamTaskVisualizer(cache: PersistentCache) : TaskVisualizer {

	@Suppress("LateinitUsage") // TODO use some kind of factory to make invalid state non-representable.
	private lateinit var graph: Graph

	@Suppress("LateinitUsage") // TODO use some kind of factory to make invalid state non-representable.
	private lateinit var viewer: Viewer

	private val options: Options = Options(cache)

	override fun showUI(settings: Settings) {
		System.setProperty("org.graphstream.ui", "swing") // 2.0
		graph = MultiGraph(settings.rootProject.name)
		try {
			val css = this::class.java.getResourceAsStream("/graphstream.css").bufferedReader().readText()
			graph.setAttribute("ui.stylesheet", css)
		} catch (ex: IOException) {
			throw IllegalStateException("Cannot read style sheet.", ex)
		}
		graph.isStrict = true
		graph.setAutoCreate(false)

		viewer = graph.display()
		val window = SwingUtilities.getWindowAncestor(viewer.defaultView as Component)
		window.createBufferStrategy(1)
		viewer.closeFramePolicy = Viewer.CloseFramePolicy.CLOSE_VIEWER
		window.addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent) {
				options.options = Options.WindowLocation(e.window)
				options.close()
			}
		})
		options.options.applyTo(viewer)
	}

	override fun initModel(graph: Map<Task, TaskData>) {
		viewer.disableAutoLayout()
		this.graph.setAttribute("layout.force", 1.0)
		for (data in graph.values) {
			val node = this.graph.addNode<Node>(id(data.task))
			node.label = data.task.path
			node.addClass(classMappingType.getValue(data.type))
			//node.setAttribute("layout.weight", @Suppress("MagicNumber") 100)
		}
		for (data in graph.values) {
			val from = this.graph.getNode<Node>(id(data.task))
			for (dep in data.depsDirect) {
				val to = this.graph.getNode<Node>(id(dep.task))
				val edge = this.graph.addEdge<Edge>(id(data.task, dep.task), from, to, true)
				edge.setAttribute("layout.weight", @Suppress("MagicNumber") 10)
			}
		}
		// See org.graphstream.ui.layout.springbox.BarnesHutLayout for what attributes it uses.
		viewer.enableAutoLayout()
	}

	override fun closeUI() {
		val view = viewer.defaultView
		if (view != null) {
			SwingUtilities.invokeLater {
				options.options = Options.WindowLocation(SwingUtilities.getWindowAncestor(view as Component))
				options.close()
				viewer.removeView(view.id)
				viewer.close()
			}
		}
	}

	override fun update(task: Task, result: TaskResult) {
		val node = graph.getNode<Node>(id(task))
		for (value in classMappingResult.values) {
			node.removeClass(value)
		}
		node.addClass(classMappingResult.getValue(result))
		//println(task.name + ": " + node.classes)
	}

	companion object {

		/** @see graphstream.css */
		private val classMappingResult: Map<TaskResult, String> =
			EnumMap<TaskResult, String>(TaskResult::class.java).apply {
				this[TaskResult.Executing] = "executing"
				this[TaskResult.Completed] = "executed"
				this[TaskResult.NoWork] = "nowork"
				this[TaskResult.Skipped] = "skipped"
				this[TaskResult.UpToDate] = "uptodate"
				this[TaskResult.Failure] = "failure"
				check(this.keys.size == TaskResult.values().size)
			}

		/** @see graphstream.css */
		private val classMappingType: Map<TaskType, String> =
			EnumMap<TaskType, String>(TaskType::class.java).apply {
				this[TaskType.Unknown] = "unknown"
				this[TaskType.Normal] = "normal"
				this[TaskType.Requested] = "requested"
				this[TaskType.Excluded] = "excluded"
				check(this.keys.size == TaskType.values().size)
			}

		@Suppress("FunctionMinLength")
		private fun id(task: Task): String =
			task.path

		@Suppress("FunctionMinLength")
		private fun id(from: Task, to: Task): String =
			id(from) + "->" + id(to)
	}
}
