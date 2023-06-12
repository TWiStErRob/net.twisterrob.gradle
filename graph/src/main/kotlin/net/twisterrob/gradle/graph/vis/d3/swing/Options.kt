package net.twisterrob.gradle.graph.vis.d3.swing

import net.twisterrob.gradle.graph.vis.VisualizerOptions
import org.gradle.cache.PersistentCache
import java.awt.Component
import java.util.Properties

internal class Options(
	cache: PersistentCache
) : VisualizerOptions<Options.WindowLocation>(cache) {

	override fun readOptions(props: Properties): WindowLocation {
		val default = createDefault()
		return WindowLocation(
			x = props.getProperty("x").toIntOr(default.x),
			y = props.getProperty("y").toIntOr(default.y),
			width = props.getProperty("width").toIntOr(default.width),
			height = props.getProperty("height").toIntOr(default.height),
		)
	}

	override fun writeOptions(options: WindowLocation): Properties =
		Properties().apply {
			this["x"] = options.x.toString()
			this["y"] = options.y.toString()
			this["width"] = options.width.toString()
			this["height"] = options.height.toString()
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

		constructor(window: Component) : this(
			x = window.location.x,
			y = window.location.y,
			width = window.size.width,
			height = window.size.height,
		)

		fun applyTo(window: Component) {
			window.setLocation(x, y)
			window.setSize(width, height)
		}
	}

	companion object {

		private fun String?.toIntOr(defaultValue: Int): Int =
			this?.toIntOrNull() ?: defaultValue
	}
}
