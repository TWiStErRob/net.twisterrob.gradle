package net.twisterrob.gradle.graph.vis.d3.javafx;

import java.util.Map;

import javafx.event.EventHandler;
import javafx.stage.*;
import net.twisterrob.gradle.graph.tasks.*;
import net.twisterrob.gradle.graph.vis.TaskVisualizer;
import net.twisterrob.gradle.graph.vis.d3.javafx.Settings.WindowLocation;
import org.gradle.api.*;
import org.gradle.cache.PersistentCache;

/**
 * @see <a href="http://stackoverflow.com/a/20125944/253468">Idea from SO</a>
 */
public class D3JavaFXTaskVisualizer implements TaskVisualizer {
	private GraphWindow window;
	private final Settings settings;

	public D3JavaFXTaskVisualizer(PersistentCache cache) {
		settings = new Settings(cache);
	}

	@Override public void showUI(org.gradle.api.initialization.Settings project) {
		JavaFXApplication.startLaunch(settings.getSettings());
		window = JavaFXApplication.show(project);
		if (window != null) {
			settings.getSettings().applyTo(window.getStage());
			window.getStage().setOnHiding(new EventHandler<WindowEvent>() {
				@Override public void handle(WindowEvent event) {
					settings.setSettings(new WindowLocation((Window)event.getSource()));
					settings.close();
				}
			});
		}
	}

	@Override public void initModel(Map<Task, TaskData> graph) {
		if (window != null) {
			window.initModel(graph);
		}
	}

	@Override public void update(Task task, TaskResult result) {
		if (window != null) {
			window.update(task, result);
		}
	}

	@Override public void closeUI() {
		if (window == null) {
			settings.close();
		}
		window = null;
		JavaFXApplication.hide();
	}
}
