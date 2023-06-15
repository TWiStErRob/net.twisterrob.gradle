package net.twisterrob.gradle.graph.vis.d3.interop

import javafx.scene.web.WebEngine
import net.twisterrob.gradle.graph.logger
import netscape.javascript.JSObject
import org.intellij.lang.annotations.Language

private val LOG = logger<JavaScriptToJavaConsoleBridge>()

/**
 * A hackier alternative to `WebConsoleListener`, which is internal, but better featured (shows line number).
 * Keeping it here, unused, in case that gets deleted.
 *
 * @see com.sun.javafx.webkit.WebConsoleListener.setDefaultListener
 */
class JavaScriptToJavaConsoleBridge private constructor(
	@Suppress("PrivatePropertyName", "ConstructorParameterNaming") // Mimic real JS code.
	private val JSON: JSObject,
) {
	/**
	 * @thread JavaFX Application Thread
	 */
	fun log(arguments: JSObject, level: String) {
		val args = arguments
			.iterator()
			.asSequence()
			.map { JSON.call("stringify", it) }
			.toList()
		LOG.trace("console.{}({})", level, args.joinToString(", "))
	}

	companion object {

		fun WebEngine.wireLogging() {
			val window = executeScript("window") as JSObject
			val java = JavaScriptToJavaConsoleBridge(executeScript("JSON") as JSObject)
			@Suppress("JSUnresolvedReference") // Global variable is created by setMember.
			val javaBridgeName = "javaConsole"
			window.setMember(javaBridgeName, java)
			val log = java::log.name
			@Language("JavaScript")
			val replaceConsoleLogging = """
				console.log = function() { ${javaBridgeName}.${log}(arguments, 'log') };
				console.debug = function() { ${javaBridgeName}.${log}(arguments, 'debug') };
				console.info = function() { ${javaBridgeName}.${log}(arguments, 'info') };
				console.warn = function() { ${javaBridgeName}.${log}(arguments, 'warn') };
				console.error = function() { ${javaBridgeName}.${log}(arguments, 'error') };
				console.trace = function() { ${javaBridgeName}.${log}(arguments, 'trace') };
			""".trimIndent()
			executeScript(replaceConsoleLogging)
		}
	}
}

private operator fun JSObject.iterator(): Iterator<Any?> =
	object : Iterator<Any?> {
		private var index: Int = 0
		private val length: Int = this@iterator.getMember("length") as Int

		override fun hasNext(): Boolean =
			index < length

		override fun next(): Any? =
			if (hasNext()) {
				this@iterator.getSlot(index++)
			} else {
				throw NoSuchElementException()
			}
	}
