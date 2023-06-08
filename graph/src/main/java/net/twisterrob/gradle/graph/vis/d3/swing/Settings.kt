package net.twisterrob.gradle.graph.vis.d3.swing

import net.twisterrob.gradle.graph.vis.VisualizerSettings
import org.gradle.cache.PersistentCache
import java.awt.Component
import java.util.Properties

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

	internal class WindowLocation(
		val x: Int,
		val y: Int,
		val width: Int,
		val height: Int,
	) {

		fun applyTo(window: Component) {
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

		private fun String?.toIntOr(defaultValue: Int): Int =
			this?.toIntOrNull() ?: defaultValue
	}
}
