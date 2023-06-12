package net.twisterrob.gradle.graph.vis.d3.interop

import javafx.application.Platform
import javafx.scene.web.WebEngine
import net.twisterrob.gradle.graph.vis.d3.Debug
import netscape.javascript.JSException
import netscape.javascript.JSObject

class JavaToJavaScriptModelBridge(engine: WebEngine) {

	private val model: JSObject

	/** @thread JavaFX Application Thread */
	init {
		model = engine.executeScript("model") as JSObject
	}

	private fun modelCall(methodName: String, vararg args: Any?) {
		val argsStr = args.contentToString()
		if (Debug.WebView) {
			message("model.${methodName}(${argsStr.replace("\\s+".toRegex(), " ").abbreviate(@Suppress("MagicNumber") 150)})")
		}
		Platform.runLater {
			/** @thread JavaFX Application Thread */
			try {
				model.call(methodName, *args)
			} catch (ex: JSException) {
				throw JSException("Failure model.${methodName}(${argsStr})").initCause(ex)
			}
		}
	}

	private fun message(message: String?) {
		System.err.println(message)
	}

	fun init(graph: String) {
		modelCall("init", graph)
	}

	fun update(name: String, result: String) {
		modelCall("update", name, result)
	}
}

private fun String.abbreviate(maxLength: Int, suffix: String = Typography.ellipsis.toString()): String =
	if (maxLength < this.length) {
		this.substring(0, maxLength) + suffix
	} else {
		this
	}
