package net.twisterrob.gradle.graph.vis.d3.swing

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import net.twisterrob.gradle.graph.vis.d3.GradleJULFixer
import org.gradle.api.Task
import org.gradle.cache.PersistentCache
import java.awt.Color
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

class D3SwingTaskVisualizer(
	cache: PersistentCache
) : net.twisterrob.gradle.graph.vis.d3.GraphWindow() {

	private val settings: Settings
	var window: JFrame? = null

	//private final GradleJULFixer fixer = new GradleJULFixer();
	init {
		settings = Settings(cache)
		SwingUtilities.invokeLater {
			/** @thread Swing Event Dispatch Thread */
			createUI()
		}
	}

	/** @thread Swing Event Dispatch Thread */
	private fun createUI() {
		window = JFrame("Gradle Build Graph").apply {
			//isUndecorated = true
			//background = Color(0, 0, 0, 0) // Transparent black.
			contentPane.background = @Suppress("MagicNumber") Color(255, 255, 255, 255) // Opaque white.
			defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
			createBufferStrategy(1)
			addWindowListener(object : WindowAdapter() {
				/** @thread Swing Event Dispatch Thread */
				override fun windowClosed(e: WindowEvent) {
					settings.settings = Settings.WindowLocation(e.window)
					settings.close()
				}
			})
		}
		val settings = settings.settings
		settings.applyTo(window!!)
		GradleJULFixer.fix()
		val fxPanel = initFX(settings)
		if (fxPanel == null) {
			window!!.dispose()
			window = null
		} else {
			window!!.add(fxPanel)
		}
	}

	private fun initFX(settings: Settings.WindowLocation): JFXPanel? {
		try {
			Platform.setImplicitExit(false)
			val fxPanel = JFXPanel()
			Platform.runLater {
				/** @thread JavaFX Application Thread */
				fxPanel.scene = createScene(settings.width.toDouble(), settings.height.toDouble())
			}
			return fxPanel
		} catch (@Suppress("TooGenericExceptionCaught") ex: RuntimeException) {
			// TooGenericExceptionCaught: want to catch everything, because JavaFX problems should not crash Gradle.
			if (ex.cause is UnsatisfiedLinkError) {
				System.err.println( // TODO logging
					"Sorry, JavaFX is clashing in Gradle daemon, "
							+ "try again after a `gradle --stop` or add `--no-daemon`\n"
							+ ex.cause.toString()
				)
			} else {
				@Suppress("PrintStackTrace") // TODO logging
				ex.printStackTrace()
			}
			return null
		}
	}

	/**
	 * JavaFX initialization may fail due to multiple classloaders trying to load glass.dll into one process.
	 * In that case just skip every operation, because there's no UI.
	 */
	private fun windowInitFailed(): Boolean =
		window == null

	override fun showUI(project: org.gradle.api.initialization.Settings) {
		super.showUI(project)
		//fixer.start()
		SwingUtilities.invokeLater {
			/** @thread Swing Event Dispatch Thread */
			if (windowInitFailed()) {
				return@invokeLater
			}
			window!!.title = "${project.rootProject.name} - Gradle Build Graph"
			window!!.isVisible = true
		}
	}

	override fun initModel(graph: Map<Task, TaskData>) {
		if (windowInitFailed()) {
			return
		}
		super.initModel(graph)
	}

	override fun update(task: Task, result: TaskResult) {
		if (windowInitFailed()) {
			return
		}
		super.update(task, result)
	}

	override fun closeUI() {
		super.closeUI()
		//fixer.interrupt()
		if (windowInitFailed()) {
			settings.close()
		}
		SwingUtilities.invokeLater {
			/** @thread Swing Event Dispatch Thread */
			if (windowInitFailed()) {
				return@invokeLater
			}
			window!!.dispose()
			window = null
		}
	}
}
