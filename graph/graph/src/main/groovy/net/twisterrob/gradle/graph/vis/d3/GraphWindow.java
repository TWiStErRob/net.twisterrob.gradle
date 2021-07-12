package net.twisterrob.gradle.graph.vis.d3;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.*;

import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.gradle.api.*;

import com.google.gson.*;

import javafx.beans.value.*;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.*;
import netscape.javascript.JSObject;

import net.twisterrob.gradle.graph.tasks.*;
import net.twisterrob.gradle.graph.vis.TaskVisualizer;
import net.twisterrob.gradle.graph.vis.d3.interop.*;

// https://blogs.oracle.com/javafx/entry/communicating_between_javascript_and_javafx
// http://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
public abstract class GraphWindow implements TaskVisualizer {
	private JavaScriptBridge bridge;

	/** @thread JavaFX Application Thread */
	protected Scene createScene(double width, double height) {
		BorderPane border = new BorderPane();
		border.setPrefWidth(width);
		border.setPrefHeight(height);
		border.setCenter(setupBrowser());
		//border.setStyle("-fx-background-color: #00FF80");
		return new Scene(border, Color.TRANSPARENT);
	}

	/** @thread JavaFX Application Thread */
	protected WebView setupBrowser() {
		WebView webView = new WebView();
		final WebEngine webEngine = webView.getEngine();
		//setBackgroundColor(getPage(webEngine));
		//webEngine.setUserStyleSheetLocation(buildCSSDataURI());
		webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
			@Override public void changed(ObservableValue<? extends State> value, State oldState,
					State newState) {
				//System.err.println(String.format("State changed: %s -> %s: %s\n", oldState, newState, value));
				@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
				Throwable ex = webEngine.getLoadWorker().getException();
				if (ex != null && newState == State.FAILED) {
					ex.printStackTrace();
				}
				if (newState == State.SUCCEEDED) {
					JSObject jsWindow = (JSObject)webEngine.executeScript("window");
					JavaScriptBridge bridge = new JavaScriptBridge(webEngine);
					webEngine.executeScript("console.log = function() { java.log(arguments) };");
					//webEngine.executeScript("console.debug = function() { java.log(arguments) };");
					jsWindow.setMember("java", bridge);

					GraphWindow.this.bridge = bridge;
				}
			}
		});
		try {
			String text = IOGroovyMethods.getText(getClass().getResourceAsStream("/d3-graph.html"));
			String base = getClass().getResource("/d3-graph.html").toExternalForm().replaceFirst("[^/]*$", "");
			text = text.replaceFirst("<head>", "<head><base href=\"" + base + "\" />");
			// TODO is load and loadContent faster?
			webEngine.load(getClass().getResource("/d3-graph.html").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return webView;
	}

	protected boolean isBrowserReady() {
		return bridge != null;
	}

	private static void setBackgroundColor(Object page) {
		if (page instanceof com.sun.webpane.platform.WebPage) {
			System.out.println("webpane.platform");
			((com.sun.webpane.platform.WebPage)page).setBackgroundColor(0x00000000);
		} else {
			System.out.println("Unknown page: " + (page != null? page.getClass() : null));
		}
	}
	private static Object getPage(WebEngine webEngine) {
		try {
			Field f = WebEngine.class.getDeclaredField("page");
			f.setAccessible(true);
			return f.get(webEngine);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	@Override public void initModel(Map<Task, TaskData> graph) {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.enableComplexMapKeySerialization()
				.registerTypeHierarchyAdapter(Task.class, new TaskSerializer())
				.registerTypeAdapter(TaskData.class, new TaskDataSerializer())
				.registerTypeAdapter(TaskType.class, new TaskTypeSerializer())
				.registerTypeAdapter(TaskResult.class, new TaskResultSerializer())
				.create();
		bridge.init(gson.toJson(graph));
	}

	@Override public void update(Task task, TaskResult result) {
		bridge.update(TaskSerializer.getKey(task), TaskResultSerializer.getState(result));
	}

	@Override public void showUI(Project project) {
		if (isBrowserReady()) {
			initModel(new HashMap<Task, TaskData>()); // reset graph before displaying it again
		}
	}

	@Override public void closeUI() {
	}

	private String buildCSSDataURI() {
		String bgColor = "rgba(0, 0, 0, 0.0)"; // "#" + Color.WHITE.toString().substring(2, 8);
		String css = "body { background: " + bgColor + "; }";
		try {
			return "data:text/css;charset=utf-8," + URLEncoder.encode(css, "utf-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException ex) {
			throw (Error)new InternalError("utf-8 encoding cannot be found?").initCause(ex);
		}
	}
}
