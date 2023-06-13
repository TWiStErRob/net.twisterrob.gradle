package net.twisterrob.gradle.graph.vis.d3

import com.google.gson.GsonBuilder
import com.vladsch.javafx.webview.debugger.DevToolsDebuggerJsBridge
import com.vladsch.javafx.webview.debugger.JfxDebuggerAccess
import com.vladsch.javafx.webview.debugger.LogHandler
import javafx.concurrent.Worker
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import net.twisterrob.gradle.graph.Debug
import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import net.twisterrob.gradle.graph.tasks.TaskType
import net.twisterrob.gradle.graph.vis.TaskVisualizer
import net.twisterrob.gradle.graph.vis.d3.interop.JavaToJavaScriptModelBridge
import net.twisterrob.gradle.graph.vis.d3.interop.TaskDataSerializer
import net.twisterrob.gradle.graph.vis.d3.interop.TaskResultSerializer
import net.twisterrob.gradle.graph.vis.d3.interop.TaskSerializer
import net.twisterrob.gradle.graph.vis.d3.interop.TaskTypeSerializer
import netscape.javascript.JSObject
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.intellij.lang.annotations.Language
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import javax.annotation.OverridingMethodsMustInvokeSuper

// https://blogs.oracle.com/javafx/entry/communicating_between_javascript_and_javafx
// http://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
@Suppress("UnnecessaryAbstractClass") // Subclasses must override some methods.
abstract class GraphWindow : TaskVisualizer {

	private var bridge: JavaToJavaScriptModelBridge? = null

	protected open val isBrowserReady: Boolean
		get() = bridge != null

	/** @thread JavaFX Application Thread
	 */
	protected fun createScene(width: Double, height: Double): Scene {
		val border = BorderPane().apply {
			prefWidth = width
			prefHeight = height
			center = setupBrowser()
			//style = "-fx-background-color: #00FF80"
		}
		return Scene(border, Color.TRANSPARENT)
	}

	/** @thread JavaFX Application Thread
	 */
	protected open fun setupBrowser(): WebView {
		val webView = WebView()
		setupDebugger(webView)
		val webEngine: WebEngine = webView.engine
		if (@Suppress("ConstantConditionIf", "RedundantSuppression") false) {
			setBackgroundColor(getPage(webEngine))
			webEngine.userStyleSheetLocation = buildCSSDataURI()
		}
		if (Debug.WebView) {
			com.sun.javafx.webkit.WebConsoleListener
				.setDefaultListener { _, message, lineNumber, sourceId ->
					fun String.relocate(): String =
						this.replace(
							"""jar:file:/.*?/graph-.*?\.jar!/""".toRegex(),
							Regex.escapeReplacement(Debug.ResourcesFolder)
						)
					println("console: ${message.relocate()} (${sourceId.relocate()}:${lineNumber})")
				}
		}
		// Used in d3-graph.html.
		(webEngine.executeScript("window") as JSObject).setMember("isJavaHosted", true)
		webEngine.loadWorker.stateProperty().addListener { value, oldState, newState ->
			if (Debug.WebView) {
				System.err.printf("State changed: %s -> %s: %s%n", oldState, newState, value)
			}
			when (newState) {
				null -> error("newState cannot be null")
				Worker.State.READY -> error("It never becomes ready, it starts there.")
				Worker.State.SCHEDULED -> { } // Normal operation
				Worker.State.RUNNING -> { } // Normal operation
				Worker.State.SUCCEEDED -> bridge = JavaToJavaScriptModelBridge(webEngine)
				Worker.State.CANCELLED -> error("Web loading cancelled")
				Worker.State.FAILED -> webEngine.loadWorker.exception?.printStackTrace()
			}
		}
		try {
			val d3Resource = javaClass.getResource("/d3-graph.html") ?: error("Cannot find d3-graph.html.")
			var text: String = d3Resource.openStream().bufferedReader().readText()
			val base: String = d3Resource.toExternalForm().replaceFirst("""[^/]*$""".toRegex(), """""")
			@Suppress("UNUSED_VALUE") // TODO why is this unused?
			text = text.replaceFirst("""<head>""".toRegex(), """<head><base href="${base}" />""")
			// TODO is load and loadContent faster?
			webEngine.load(d3Resource.toExternalForm())
		} catch (ex: IOException) {
			@Suppress("PrintStackTrace") // TODO logging
			ex.printStackTrace()
		}
		return webView
	}

private fun setupDebugger(webView: WebView) {
	// Patch default LogHandler... the initialization order is wrong: LOG_HANDLER forward-references NULL.
	//LogHandler.LOG_HANDLER = LogHandler.NULL
	// Optionally override LogHandler with my own implementation to log wherever I want it to route.
	LogHandler.LOG_HANDLER = @Suppress("StringLiteralDuplication") object : LogHandler() {
		// @formatter:off
		override fun error(message: String) { log("error", message) }
		override fun error(message: String, t: Throwable) { log("error", message, t) }
		override fun error(t: Throwable) { log("error", null, t) }
		override fun info(message: String) { log("info", message) }
		override fun info(message: String, t: Throwable) { log("info", message, t) }
		override fun info(t: Throwable) { log("info", null, t) }
		override fun warn(message: String) { log("warn", message) }
		override fun warn(message: String, t: Throwable) { log("warn", message, t) }
		override fun warn(t: Throwable) { log("warn",null, t) }
		// Disable debug logging in JfxWebSocketServer about the Dev Tools protocol,
		// because it causes unconditional logs to System.out/err rather than to debug().
		override fun isDebugEnabled(): Boolean = false
		override fun debug(message: String) { log("debug", message) }
		override fun debug(message: String, t: Throwable) { log("debug", message, t) }
		override fun debug(t: Throwable) { log("debug", null, t) }
		override fun isTraceEnabled(): Boolean = true
		override fun trace(message: String) { log("trace", message) }
		override fun trace(message: String, t: Throwable) { log("trace", message, t) }
		override fun trace(t: Throwable) { log("trace", null, t) }
		// @formatter:on
		private fun log(level: String, message: String? = null, t: Throwable? = null) {
			println("[$level] $message")
			t?.printStackTrace()
		}
	}
	val bridge = DevToolsDebuggerJsBridge(webView, webView.engine, 0, null, false)
	webView.engine.loadWorker.stateProperty().addListener { _, _, newState ->
		when (newState) {
			Worker.State.SCHEDULED -> {
				// According to docs, there's no need for custom load() method,
				// this event will fire at the right time.
				bridge.pageReloading()
			}
			Worker.State.RUNNING -> {
				// Required since Debugger.globalObjectCleared appears to be not called.
				webView.engine.executeScript(
					bridge::class.java
						.getDeclaredField("myJfxDebuggerAccess")
						.apply { isAccessible = true }
						.get(bridge)
						.let { it as JfxDebuggerAccess }
						.jsBridgeHelperScript()
				)
			}
			Worker.State.SUCCEEDED -> {
				bridge.connectJsBridge()
				val port = @Suppress("MagicNumber") 9222
				bridge.startDebugServer(port, Throwable::printStackTrace) {
					// Note: URL protocol changed at one point from chrome-devtools:// to devtools://.
					println("Ready at devtools://devtools/bundled/inspector.html?ws=localhost:${port}")
				}
			}
			else -> {} // Nothing to do.
		}
	}
}

	@OverridingMethodsMustInvokeSuper
	override fun initModel(graph: Map<Task, TaskData>) {
		val gson = GsonBuilder()
			.setPrettyPrinting()
			.enableComplexMapKeySerialization()
			.registerTypeHierarchyAdapter(Task::class.java, TaskSerializer())
			.registerTypeAdapter(TaskData::class.java, TaskDataSerializer())
			.registerTypeAdapter(TaskType::class.java, TaskTypeSerializer())
			.registerTypeAdapter(TaskResult::class.java, TaskResultSerializer())
			.create()
		bridge?.init(gson.toJson(graph))
	}

	@OverridingMethodsMustInvokeSuper
	override fun update(task: Task, result: TaskResult) {
		bridge?.update(TaskSerializer.getKey(task), TaskResultSerializer.getState(result))
	}

	@OverridingMethodsMustInvokeSuper
	override fun showUI(settings: Settings) {
		if (isBrowserReady) {
			initModel(emptyMap()) // Reset graph before displaying it again.
		}
	}

	@OverridingMethodsMustInvokeSuper
	override fun closeUI() {
		// Optional implementation, default: do nothing.
	}

	private fun buildCSSDataURI(): String {
		@Language("css")
		val css = """body { background: rgba(0, 0, 0, 0.0); }"""
		//val css = """body { background: #${Color.WHITE.toString().substring(2, 8)}; }"""
		try {
			return "data:text/css;charset=utf-8," + URLEncoder.encode(css, "utf-8").replace("\\+".toRegex(), "%20")
		} catch (ex: UnsupportedEncodingException) {
			throw InternalError("utf-8 encoding cannot be found?").initCause(ex)
		}
	}

	companion object {

		private fun setBackgroundColor(page: Any?) {
			if (page is com.sun.webkit.WebPage) {
				@Suppress("ForbiddenMethodCall") // TODO logging
				println("webpane.platform")
				page.setBackgroundColor(0x00000000)
			} else {
				@Suppress("ForbiddenMethodCall") // TODO logging
				println("Unknown page: " + page?.javaClass)
			}
		}

		private fun getPage(webEngine: WebEngine): Any? =
			try {
				WebEngine::class.java
					.getDeclaredField("page")
					.apply { isAccessible = true }
					.get(webEngine)
			} catch (ex: NoSuchFieldException) {
				@Suppress("PrintStackTrace") // TODO logging
				ex.printStackTrace()
				null
			} catch (ex: IllegalAccessException) {
				@Suppress("PrintStackTrace") // TODO logging
				ex.printStackTrace()
				null
			}
	}
}
