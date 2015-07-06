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

// https://blogs.oracle.com/javafx/entry/communicating_between_javascript_and_javafx
// http://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
public class GraphWindow implements TaskVisualizer {
	private final Stage window;
	private JavaBridge bridge;

	public GraphWindow(Stage stage) {
		this.window = stage;
		BorderPane border = new BorderPane();
		Scene scene = new Scene(border, Color.DODGERBLUE);
		WebView webView = new WebView();
		border.setCenter(webView);
		setupBrowser(webView.getEngine());

		// http://stackoverflow.com/a/9405733/253468
		//webView.getEngine().executeScript(FIREBUG_LITE);

		stage.setTitle("Loading...");
		stage.setScene(scene);
		Screen screen = Screen.getScreens().get(0);
		Rectangle2D bounds = screen.getVisualBounds();

		stage.setX(bounds.getMinX());
		stage.setY(bounds.getMinY());
		stage.setWidth(bounds.getWidth());
		stage.setHeight(bounds.getHeight());
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
					bridge = new JavaBridge(webEngine);
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
		window.show();
	}

	@Override public void initModel(Map<Task, TaskData> graph) {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.enableComplexMapKeySerialization()
				.registerTypeHierarchyAdapter(Task.class, new TaskSerializer())
				.registerTypeAdapter(TaskData.class, new TaskDataSerializer())
				.registerTypeAdapter(TaskType.class, new TaskTypeSerializer())
				.registerTypeAdapter(TaskResult.class, new TaskStateSerializer())
				.create();
		bridge.init(gson.toJson(graph));
	}

	@Override public void update(Task task, TaskResult result) {
		bridge.update(task.getName(), TaskStateSerializer.getState(result));
	}

	@Override public void closeUI() {
		window.close();
	}

	public static class JavaBridge {
		private final JSObject JSON;
		private final JSObject model;
		public JavaBridge(WebEngine engine) {
			this.JSON = (JSObject)engine.executeScript("JSON");
			this.model = (JSObject)engine.executeScript("model");
		}
		private void modelCall(final String methodName, final Object... args) {
			final String argsStr = Arrays.toString(args);
			final String argsShort = argsStr.length() < 50? argsStr
					: argsStr.substring(0, 50).replaceAll("\\s+", " ") + "...";
			//message(methodName + "(" + argsShort + ")");
			Platform.runLater(new Runnable() {
				@Override public void run() {
					try {
						model.call(methodName, args);
					} catch (RuntimeException ex) {
						throw new RuntimeException("Failure " + methodName + "(" + argsStr + ")", ex);
					}
				}
			});
		}
		public void message(String message) {
			System.err.println(message);
		}

		@SuppressWarnings("unused")
		public void log(JSObject args) {
			for (int i = 0, len = (Integer)args.getMember("length"); i < len; i++) {
				Object arg = args.getSlot(i);
				System.err.print(JSON.call("stringify", new Object[] {arg}));
				if (i < len - 1) {
					System.err.print(", ");
				}
			}
			System.err.println();
		}

		public void init(String graph) {
			modelCall("init", graph);
		}
		public void update(String name, String result) {
			modelCall("update", name, result);
		}
	}
}
