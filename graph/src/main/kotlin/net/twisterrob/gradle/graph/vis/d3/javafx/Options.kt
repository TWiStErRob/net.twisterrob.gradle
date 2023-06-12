package net.twisterrob.gradle.graph.vis.d3.javafx

import javafx.stage.Window
import net.twisterrob.gradle.graph.vis.VisualizerOptions
import org.gradle.cache.PersistentCache
import java.util.Properties

internal class Options(
	cache: PersistentCache
) : VisualizerOptions<Options.WindowLocation>(cache) {

	override fun readOptions(props: Properties): WindowLocation {
		val default = createDefault()
		return WindowLocation(
			x = props.getProperty("x").toDoubleOr(default.x),
			y = props.getProperty("y").toDoubleOr(default.y),
			width = props.getProperty("width").toDoubleOr(default.width),
			height = props.getProperty("height").toDoubleOr(default.height),
		)
	}

	override fun writeOptions(options: WindowLocation): Properties =
		Properties().apply {
			this["x"] = options.x.toString()
			this["y"] = options.y.toString()
			this["width"] = options.width.toString()
			this["height"] = options.height.toString()
		}

	override fun createDefault(): WindowLocation {
		return WindowLocation(
			x = 0.0,
			y = 0.0,
			width = 800.0,
			height = 600.0,
		)
	}

	class WindowLocation(
		val x: Double,
		val y: Double,
		val width: Double,
		val height: Double,
	) {

		constructor(window: Window) : this(
			x = window.x,
			y = window.y,
			width = window.width,
			height = window.height,
		)

		fun applyTo(window: Window) {
			window.width = width
			window.height = height
			window.x = x
			window.y = y
		}
	}

	companion object {

		private fun String?.toDoubleOr(defaultValue: Double): Double =
			this?.toDoubleOrNull() ?: defaultValue
	}
}
