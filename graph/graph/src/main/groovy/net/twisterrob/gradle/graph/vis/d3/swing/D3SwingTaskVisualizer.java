package net.twisterrob.gradle.graph.vis.d3.swing;

import java.awt.Color;
import java.awt.event.*;
import java.util.Map;

import javax.swing.*;

import org.gradle.api.*;
import org.gradle.cache.PersistentCache;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import net.twisterrob.gradle.graph.tasks.*;
import net.twisterrob.gradle.graph.vis.d3.GradleJULFixer;
import net.twisterrob.gradle.graph.vis.d3.swing.Settings.WindowLocation;

public class D3SwingTaskVisualizer extends net.twisterrob.gradle.graph.vis.d3.GraphWindow {

	private final Settings settings;
	public JFrame window;
	//private final GradleJULFixer fixer = new GradleJULFixer();

	public D3SwingTaskVisualizer(PersistentCache cache) {
		settings = new Settings(cache);
		SwingUtilities.invokeLater(new Runnable() {
			/** @thread Swing Event Dispatch Thread */
			@Override public void run() {
				createUI();
			}
		});
	}

	/** @thread Swing Event Dispatch Thread */
	private void createUI() {
		window = new JFrame("Gradle Build Graph");
		//window.setUndecorated(true);
		//window.setBackground(new Color(0, 0, 0, 0));
		window.getContentPane().setBackground(new Color(255, 255, 255, 255));
		window.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		window.createBufferStrategy(1);
		window.addWindowListener(new WindowAdapter() {
			/** @thread Swing Event Dispatch Thread */
			@Override public void windowClosed(WindowEvent e) {
				settings.setSettings(new WindowLocation(e.getWindow()));
				settings.close();
			}
		});
		WindowLocation settings = this.settings.getSettings();
		settings.applyTo(window);
		GradleJULFixer.fix();
		JFXPanel fxPanel = initFX(settings);
		if (fxPanel == null) {
			window.dispose();
			window = null;
		} else {
			window.add(fxPanel);
		}
	}

	private JFXPanel initFX(final WindowLocation settings) {
		try {
			Platform.setImplicitExit(false);
			final JFXPanel fxPanel = new JFXPanel();
			Platform.runLater(new Runnable() {
				/** @thread JavaFX Application Thread */
				@Override public void run() {
					fxPanel.setScene(createScene(settings.width, settings.height));
				}
			});
			return fxPanel;
		} catch (RuntimeException ex) {
			if (ex.getCause() instanceof UnsatisfiedLinkError) {
				System.err.println("Sorry, JavaFX is clashing in Gradle daemon, "
						+ "try again after a `gradle --stop` or add `--no-daemon`\n"
						+ ex.getCause().toString());
			} else {
				ex.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * JavaFX initialization may fail due to multiple classloaders trying to load glass.dll into one process.
	 * In that case just skip every operation, because there's no UI.
	 */
	private boolean windowInitFailed() {
		return window == null;
	}

	@Override public void showUI(final Project project) {
		super.showUI(project);
		//fixer.start();
		SwingUtilities.invokeLater(new Runnable() {
			/** @thread Swing Event Dispatch Thread */
			@Override public void run() {
				if (windowInitFailed()) {
					return;
				}
				window.setTitle(String.format("%s - Gradle Build Graph", project.getName()));
				window.setVisible(true);
			}
		});
	}

	@Override public void initModel(Map<Task, TaskData> graph) {
		if (windowInitFailed()) {
			return;
		}
		super.initModel(graph);
	}

	@Override public void update(Task task, TaskResult result) {
		if (windowInitFailed()) {
			return;
		}
		super.update(task, result);
	}

	@Override public void closeUI() {
		super.closeUI();
		//fixer.interrupt();
		if (windowInitFailed()) {
			settings.close();
		}
		SwingUtilities.invokeLater(new Runnable() {
			/** @thread Swing Event Dispatch Thread */
			@Override public void run() {
				if (windowInitFailed()) {
					return;
				}
				window.dispose();
				window = null;
			}
		});
	}
}
