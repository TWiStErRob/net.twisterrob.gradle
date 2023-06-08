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
			x = parse(props.getProperty("x"), default.x),
			y = parse(props.getProperty("y"), default.y),
			width = parse(props.getProperty("width"), default.width),
			height = parse(props.getProperty("height"), default.height),
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

		fun applyTo(viewer: Viewer) {
			val window = SwingUtilities.getWindowAncestor(viewer.defaultView) as JFrame
			window.setLocation(x, y)
			window.setSize(width, height)
		}

		constructor(window: Component) : this(
			x = window.location.x,
			y = window.location.y,
			width = window.size.width,
			height = window.size.height,
		)
	}

	companion object {

		private fun parse(value: String?, defaultValue: Int): Int =
			try {
				value?.toInt() ?: defaultValue
			} catch (ex: NumberFormatException) {
				defaultValue
			}
	}
}
