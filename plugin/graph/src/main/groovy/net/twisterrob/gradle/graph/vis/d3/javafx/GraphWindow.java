package net.twisterrob.gradle.graph.vis.d3.javafx;

import org.gradle.api.Project;

import javafx.application.Platform;
import javafx.stage.Stage;

public class GraphWindow extends net.twisterrob.gradle.graph.vis.d3.GraphWindow {
	private final Stage window;

	public GraphWindow(Stage stage) {
		stage.setScene(createScene(stage.getWidth(), stage.getHeight()));
		this.window = stage;

		window.setTitle("Gradle Build Graph");
	}

	public Stage getStage() {
		return window;
	}

	@Override public void showUI(Project project) {
		super.showUI(project);
		window.setTitle(String.format("%s - Gradle Build Graph", project.getName()));
		Platform.runLater(new Runnable() {
			@Override public void run() {
				window.show(); // delay display so that bridge's runLater runs first
			}
		});
	}

	@Override public void closeUI() {
		super.closeUI();
		Platform.runLater(new Runnable() {
			@Override public void run() {
				window.close();
			}
		});
	}
}
