package net.twisterrob.gradle.graph.vis.d3

import com.google.gson.GsonBuilder
import javafx.concurrent.Worker
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import net.twisterrob.gradle.graph.logger
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

private val LOG = logger<GraphWindow>()

// https://blogs.oracle.com/javafx/entry/communicating_between_javascript_and_javafx
// http://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
@Suppress("detekt.UnnecessaryAbstractClass") // Subclasses must override some methods.
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
		val webEngine: WebEngine = webView.engine
		if (@Suppress("ConstantConditionIf", "RedundantSuppression") false) {
			setBackgroundColor(getPage(webEngine))
			webEngine.userStyleSheetLocation = buildCSSDataURI()
		}
		if (LOG.isDebugEnabled) {
			com.sun.javafx.webkit.WebConsoleListener
				.setDefaultListener { _, message, lineNumber, sourceId: String? ->
					/** Enables clickable links from logs. */
					fun String.relocate(): String =
						this.replace(
							"""jar:file:/.*?/graph-.*?\.jar!/""".toRegex(),
							// TODO extract somehow
							Regex.escapeReplacement("""P:\projects\workspace\net.twisterrob.gradle\graph\src\main\resources\""")
						)
					LOG.debug("console: ${message.relocate()} (${sourceId?.relocate()}:${lineNumber})")
				}
		}
		// Used in d3-graph.html.
		(webEngine.executeScript("window") as JSObject).setMember("isJavaHosted", true)
		webEngine.loadWorker.stateProperty().addListener { value, oldState, newState ->
			LOG.debug("WebView state changed: {} -> {}: {}", oldState, newState, value)
			when (newState) {
				null -> error("newState cannot be null")
				Worker.State.READY -> error("It never becomes ready, it starts there.")
				Worker.State.SCHEDULED -> { } // Normal operation
				Worker.State.RUNNING -> { } // Normal operation
				Worker.State.SUCCEEDED -> bridge = JavaToJavaScriptModelBridge(webEngine)
				Worker.State.CANCELLED -> error("Web loading cancelled.")
				Worker.State.FAILED -> LOG.error("Couldn't load page.", webEngine.loadWorker.exception)
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
			LOG.error("Failed to load d3-graph.html from resources.", ex)
		}
		return webView
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
				LOG.trace("webpane.platform")
				page.setBackgroundColor(0x00000000)
			} else {
				LOG.warn("Unknown page: {}", page?.javaClass)
			}
		}

		private fun getPage(webEngine: WebEngine): Any? =
			try {
				WebEngine::class.java
					.getDeclaredField("page")
					.apply { isAccessible = true }
					.get(webEngine)
			} catch (ex: NoSuchFieldException) {
				LOG.error("Cannot find WebEngine.page", ex)
				null
			} catch (ex: IllegalAccessException) {
				LOG.error("Cannot read WebEngine.page", ex)
				null
			}
	}
}
