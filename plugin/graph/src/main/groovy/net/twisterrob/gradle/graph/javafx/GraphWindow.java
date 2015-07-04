package net.twisterrob.gradle.graph.javafx;

import java.io.IOException;
import java.util.*;

import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.gradle.api.*;

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

import net.twisterrob.gradle.graph.TaskVisualizer.TaskState;

// https://blogs.oracle.com/javafx/entry/communicating_between_javascript_and_javafx
// http://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
public class GraphWindow {
	private final Stage window;
	private Project project;
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

	public void show(Project project) {
		window.setTitle(String.format("%s - Gradle Build Graph", project.getName()));
		window.show();
	}

	public void hide() {
		window.close();
	}

	public void addTask(Task task) {
		bridge.newNode(task.getName());
	}

	private static final EnumMap<TaskState, String> classMapping = new EnumMap<TaskState, String>(TaskState.class);

	static {
		classMapping.put(TaskState.unknown, "unknown");
		classMapping.put(TaskState.requested, "requested");
		classMapping.put(TaskState.excluded, "excluded");
		classMapping.put(TaskState.executing, "executing");
		classMapping.put(TaskState.executed, "executed");
		classMapping.put(TaskState.result_nowork, "nowork");
		classMapping.put(TaskState.result_skipped, "skipped");
		classMapping.put(TaskState.result_uptodate, "uptodate");
		classMapping.put(TaskState.result_failure, "failure");
	}

	public void setClasses(Task task, EnumSet<TaskState> addStates, EnumSet<TaskState> removeStates) {
		addTask(task);
		String[] addClasses = new String[addStates.size()];
		String[] removeClasses = new String[removeStates.size()];

		int addIndex = 0;
		for (TaskState state : addStates) {
			addClasses[addIndex++] = classMapping.get(state);
		}
		int removeIndex = 0;
		for (TaskState state : removeStates) {
			removeClasses[removeIndex++] = classMapping.get(state);
		}
		bridge.updateClasses(task.getName(), addClasses, removeClasses);
	}

	public void addDependency(Task from, Task to) {
		addTask(from);
		addTask(to);
		bridge.addLink(from.getName(), to.getName());
	}

	public static class JavaBridge {
		private final JSObject JSON;
		private final JSObject model;
		public JavaBridge(WebEngine engine) {
			this.JSON = (JSObject)engine.executeScript("JSON");
			this.model = (JSObject)engine.executeScript("model");
		}
		private void modelCall(final String methodName, final Object... args) {
			Platform.runLater(new Runnable() {
				@Override public void run() {
					model.call(methodName, args);
				}
			});
		}
		public void message(String message) {
			System.err.println(message);
		}
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
		public void newNode(String name) {
			modelCall("add", name);
		}
		public void updateClasses(String name, String[] addClasses, String[] removeClasses) {
			modelCall("updateClasses", name, addClasses, removeClasses);
		}
		public void addLink(String from, String to) {
			modelCall("depends", from, to);
		}
	}
}
