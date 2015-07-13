package net.twisterrob.gradle.graph.javafx;

import java.util.Map;

import org.gradle.api.*;
import org.gradle.cache.PersistentCache;

import net.twisterrob.gradle.graph.*;
import net.twisterrob.gradle.graph.javafx.JavaFXD3Settings.WindowLocation;

/**
 * @see <a href="http://stackoverflow.com/a/20125944/253468">Idea from SO</a>
 */
public class JavaFXTaskVisualizer implements TaskVisualizer {
	private D3GraphWindow window;
	private final JavaFXD3Settings settings;

	public JavaFXTaskVisualizer(PersistentCache cache) {
		settings = new JavaFXD3Settings(cache);
	}

	@Override public void showUI(Project project) {
		JavaFXApplication.startLaunch();
		window = JavaFXApplication.show(project);
		settings.getSettings().applyTo(window.getStage());
	}

	@Override public void initModel(Map<Task, TaskData> graph) {
		window.initModel(graph);
	}

	@Override public void update(Task task, TaskResult result) {
		window.update(task, result);
	}

	@Override public void closeUI() {
		settings.setSettings(new WindowLocation(window.getStage()));
		window = null;
		JavaFXApplication.hide();
	}
}
