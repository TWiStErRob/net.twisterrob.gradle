package net.twisterrob.gradle.graph.vis.graphstream

import net.twisterrob.gradle.graph.vis.VisualizerSettings
import org.gradle.cache.PersistentCache
import org.graphstream.ui.view.Viewer
import java.awt.Component
import java.util.Properties
import javax.swing.JFrame
import javax.swing.SwingUtilities

internal class Settings(
	cache: PersistentCache
) : VisualizerSettings<Settings.WindowLocation>(cache) {

	override fun readSettings(props: Properties): WindowLocation {
		val default = createDefault()
		return WindowLocation(
			x = props.getProperty("x").toIntOr(default.x),
			y = props.getProperty("y").toIntOr(default.y),
			width = props.getProperty("width").toIntOr(default.width),
			height = props.getProperty("height").toIntOr(default.height),
		)
	}

	override fun writeSettings(settings: WindowLocation): Properties =
		Properties().apply {
			this["x"] = settings.x.toString()
			this["y"] = settings.y.toString()
			this["width"] = settings.width.toString()
			this["height"] = settings.height.toString()
		}

	override fun createDefault(): WindowLocation =
		WindowLocation(
			x = 0,
			y = 0,
			width = 800,
			height = 600,
		)

	class WindowLocation(
		val x: Int,
		val y: Int,
		val width: Int,
		val height: Int,
	) {

		constructor(window: Component) : this(
			x = window.location.x,
			y = window.location.y,
			width = window.size.width,
			height = window.size.height,
		)

		fun applyTo(viewer: Viewer) {
			val window = SwingUtilities.getWindowAncestor(viewer.defaultView as Component) as JFrame
			window.setLocation(x, y)
			window.setSize(width, height)
		}
	}

	companion object {

		private fun String?.toIntOr(defaultValue: Int): Int =
			this?.toIntOrNull() ?: defaultValue
	}
}
