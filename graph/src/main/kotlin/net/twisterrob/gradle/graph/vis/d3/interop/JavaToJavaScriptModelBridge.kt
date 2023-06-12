package net.twisterrob.gradle.graph.vis.d3.interop

import javafx.application.Platform
import javafx.scene.web.WebEngine
import net.twisterrob.gradle.graph.Debug
import netscape.javascript.JSException
import netscape.javascript.JSObject
import org.intellij.lang.annotations.Language

class JavaToJavaScriptModelBridge(engine: WebEngine) {

	private val model: JSObject

	/** @thread JavaFX Application Thread */
	init {
		model = engine.executeScript("model") as JSObject
		model.setMember("isTraceCalls", Debug.JavaScriptInterop) // Used in javafy.
		engine.executeScript(javafy)
	}

	private fun modelCall(methodName: String, vararg args: Any?) {
		val argsStr = args.contentToString()
		if (Debug.JavaScriptInterop) {
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

	companion object {
		@Language("JavaScript")
		private val javafy = """
			model = function javafy(model) {
				function javaEntry(name, fun) {
					return function() {
						// Member created in JavaToJavaScriptModelBridge constructor.
						// noinspection JSUnresolvedReference
						if (model.isTraceCalls === true) {
							console.trace(`${'$'}{name}(${'$'}{JSON.stringify(arguments)})`);
						}
						try {
							return fun.apply(this, arguments);
						} catch (e) {
							console.warn(`${'$'}{name}(...) -> ${'$'}{e.name}: ${'$'}{e.message} ${'$'}{e.stack}`);
						}
					};
				}
				for (const item in model) {
					if (typeof model[item] === 'function') {
						model[item] = javaEntry(item, model[item]);
					}
				}
				return model;
			}(model);
		""".trimIndent()
	}
}

private fun String.abbreviate(maxLength: Int, suffix: String = Typography.ellipsis.toString()): String =
	if (maxLength < this.length) {
		this.substring(0, maxLength) + suffix
	} else {
		this
	}
