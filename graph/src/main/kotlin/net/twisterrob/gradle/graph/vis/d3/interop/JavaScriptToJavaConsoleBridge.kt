package net.twisterrob.gradle.graph.vis.d3.interop

import javafx.scene.web.WebEngine
import netscape.javascript.JSObject
import org.intellij.lang.annotations.Language

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
