package net.twisterrob.gradle.graph.vis.d3.interop

import javafx.application.Platform
import javafx.scene.web.WebEngine
import netscape.javascript.JSException
import netscape.javascript.JSObject

class JavaScriptBridge(engine: WebEngine) {

	@Suppress("PrivatePropertyName", "VariableNaming") // Mimic real JS code.
	private val JSON: JSObject
	private val model: JSObject

	/** @thread JavaFX Application Thread */
	init {
		JSON = engine.executeScript("JSON") as JSObject
		model = engine.executeScript("model") as JSObject
		engine.executeScript("console.log = function() { java.log(arguments, 'log') };")
		engine.executeScript("console.info = function() { java.log(arguments, 'info') };")
		engine.executeScript("console.warn = function() { java.log(arguments, 'warn') };")
		engine.executeScript("console.error = function() { java.log(arguments, 'error') };")
		engine.executeScript("console.trace = function() { java.log(arguments, 'trace') };")
		if (DEBUG) {
			engine.executeScript("console.debug = function() { java.log(arguments, 'debug') };");
		}
	}

	private fun modelCall(methodName: String, vararg args: Any?) {
		val argsStr = args.contentToString()
		if (DEBUG) {
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

	/**
	 * Called from d3-graph.html because [init] overrode console.* calls.
	 *
	 * @thread JavaFX Application Thread
	 */
	fun log(args: JSObject, level: String) {
		System.err.printf("console.%s(", level)
		var i = 0
		val len = args.getMember("length") as Int
		while (i < len) {
			val arg = args.getSlot(i)
			System.err.print(JSON.call("stringify", arg))
			if (i < len - 1) {
				System.err.print(", ")
			}
			i++
		}
		System.err.printf(")%n")
	}

	fun init(graph: String) {
		modelCall("init", graph)
	}

	fun update(name: String, result: String) {
		modelCall("update", name, result)
	}

	companion object {
		const val DEBUG: Boolean = true
	}
}

private fun String.abbreviate(maxLength: Int, suffix: String = Typography.ellipsis.toString()): String =
	if (maxLength < this.length) {
		this.substring(0, maxLength) + suffix
	} else {
		this
	}
