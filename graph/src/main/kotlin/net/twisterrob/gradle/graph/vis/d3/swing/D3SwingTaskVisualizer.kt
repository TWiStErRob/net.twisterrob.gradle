package net.twisterrob.gradle.graph.vis.d3.swing

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import net.twisterrob.gradle.graph.logger
import net.twisterrob.gradle.graph.tasks.TaskData
import net.twisterrob.gradle.graph.tasks.TaskResult
import net.twisterrob.gradle.graph.vis.d3.GradleJULFixer
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.gradle.cache.PersistentCache
import java.awt.Color
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

private val LOG = logger<D3SwingTaskVisualizer>()

class D3SwingTaskVisualizer(
	cache: PersistentCache
) : net.twisterrob.gradle.graph.vis.d3.GraphWindow() {

	private val options: Options
	var window: JFrame? = null

	//private final GradleJULFixer fixer = new GradleJULFixer();
	init {
		options = Options(cache)
		SwingUtilities.invokeLater {
			/** @thread Swing Event Dispatch Thread */
			window = createUI()
		}
	}

	/** @thread Swing Event Dispatch Thread */
	private fun createUI(): JFrame? {
		val window = JFrame("Gradle Build Graph").apply {
			//isUndecorated = true
			//background = Color(0, 0, 0, 0) // Transparent black.
			contentPane.background = @Suppress("detekt.MagicNumber") Color(255, 255, 255, 255) // Opaque white.
			defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
			createBufferStrategy(1)
			addWindowListener(object : WindowAdapter() {
				/** @thread Swing Event Dispatch Thread */
				override fun windowClosed(e: WindowEvent) {
					options.options = Options.WindowLocation(e.window)
					options.close()
				}
			})
		}
		val settings = options.options
		settings.applyTo(window)
		GradleJULFixer.fix()
		val fxPanel = initFX(settings)
		if (fxPanel == null) {
			window.dispose()
			return null
		}
		window.add(fxPanel)
		return window
	}

	private fun initFX(options: Options.WindowLocation): JFXPanel? {
		try {
			Platform.setImplicitExit(false)
			val fxPanel = JFXPanel()
			Platform.runLater {
				/** @thread JavaFX Application Thread */
				fxPanel.scene = createScene(options.width.toDouble(), options.height.toDouble())
			}
			return fxPanel
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: RuntimeException) {
			// TooGenericExceptionCaught: that's what JavaFX declares.
			// TooGenericExceptionCaught: want to catch everything, because JavaFX problems should not crash Gradle.
			if (ex.cause is UnsatisfiedLinkError) {
				LOG.error(
					"Sorry, JavaFX is clashing in Gradle daemon, try again after a `gradle --stop` or add `--no-daemon`",
					ex
				)
			} else {
				LOG.error("launching, failed", ex)
			}
			return null
		}
	}

	/**
	 * JavaFX initialization may fail due to multiple classloaders trying to load glass.dll into one process.
	 * In that case just skip every operation, because there's no UI.
	 */
	private inline fun withValidWindow(block: (JFrame) -> Unit) {
		val window = this.window ?: return
		block(window)
	}

	override fun showUI(settings: Settings) {
		super.showUI(settings)
		//fixer.start()
		SwingUtilities.invokeLater {
			/** @thread Swing Event Dispatch Thread */
			withValidWindow { window ->
				window.title = "${settings.rootProject.name} - Gradle Build Graph"
				window.isVisible = true
			}
		}
	}

	override fun initModel(graph: Map<Task, TaskData>) {
		withValidWindow {
			super.initModel(graph)
		}
	}

	override fun update(task: Task, result: TaskResult) {
		withValidWindow {
			super.update(task, result)
		}
	}

	override fun closeUI() {
		super.closeUI()
		//fixer.interrupt()
		options.close()
		SwingUtilities.invokeLater {
			/** @thread Swing Event Dispatch Thread */
			withValidWindow(JFrame::dispose)
			window = null
		}
	}
}
