package net.twisterrob.gradle.graph.javafx;

import java.io.IOException;
import java.util.*;

import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.gradle.api.*;

import com.google.gson.*;

import javafx.application.Platform;
import javafx.beans.value.*;
import javafx.concurrent.Worker.State;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.*;
import javafx.stage.*;
import netscape.javascript.JSObject;

import net.twisterrob.gradle.graph.*;
import net.twisterrob.gradle.graph.javafx.interop.*;

// https://blogs.oracle.com/javafx/entry/communicating_between_javascript_and_javafx
// http://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
public class D3GraphWindow implements TaskVisualizer {
	private final Stage window;
	private JavaScriptBridge bridge;

	public D3GraphWindow(Stage stage) {
		this.window = stage;
		BorderPane border = new BorderPane();
		Scene scene = new Scene(border, Color.DODGERBLUE);
		WebView webView = new WebView();
		border.setCenter(webView);
		setupBrowser(webView.getEngine());

		stage.setTitle("Loading...");
		stage.setScene(scene);
		Screen screen = Screen.getScreens().get(0);
		Rectangle2D bounds = screen.getVisualBounds();

		stage.setX(bounds.getMinX());
		stage.setY(bounds.getMinY());
		stage.setWidth(bounds.getWidth());
		stage.setHeight(bounds.getHeight());
	}

	public Stage getStage() {
		return window;
	}

	private void setupBrowser(final WebEngine webEngine) {
		webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
			@Override public void changed(ObservableValue<? extends State> value, State oldState, State newState) {
				//System.err.println(String.format("State changed: %s -> %s: %s\n", oldState, newState, value));
				@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
				Throwable ex = webEngine.getLoadWorker().getException();
				if (ex != null && newState == State.FAILED) {
					ex.printStackTrace();
				}
				if (newState == State.SUCCEEDED) {
					JSObject window = (JSObject)webEngine.executeScript("window");
					bridge = new JavaScriptBridge(webEngine);
					window.setMember("java", bridge);
					webEngine.executeScript("console.log = function() { java.log(arguments) };");
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
	}

	@Override public void showUI(Project project) {
		window.setTitle(String.format("%s - Gradle Build Graph", project.getName()));
		if (bridge != null) {
			initModel(new HashMap<Task, TaskData>()); // reset graph before displaying it again
		}
		Platform.runLater(new Runnable() {
			@Override public void run() {
				window.show(); // delay display so that bridge's runLater runs first
			}
		});
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

	@Override public void closeUI() {
		window.close();
	}
}
