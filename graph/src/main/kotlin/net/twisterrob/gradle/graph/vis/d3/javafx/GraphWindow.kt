package net.twisterrob.gradle.graph.vis.d3.javafx

import javafx.application.Platform
import javafx.stage.Stage
import net.twisterrob.gradle.graph.vis.d3.GraphWindow
import org.gradle.api.initialization.Settings

class GraphWindow(
	val stage: Stage
) : GraphWindow() {

	init {
		stage.scene = createScene(stage.width, stage.height)
		stage.title = "Gradle Build Graph"
	}

	override fun showUI(project: Settings) {
		super.showUI(project)
		stage.title = "${project.rootProject.name} - Gradle Build Graph"
		// Delay display so that bridge's runLater runs first.
		Platform.runLater(stage::show)
	}

	override fun closeUI() {
		super.closeUI()
		Platform.runLater(stage::close)
	}
}
