package net.twisterrob.gradle.graph.javafx;

import java.util.concurrent.CountDownLatch;

import org.gradle.api.Project;

import javafx.application.*;
import javafx.stage.Stage;

public class JavaFXApplication extends Application {
	private static CountDownLatch initialized;
	private static volatile JavaFXApplication app;

	private final GradleJULFixer fixer = new GradleJULFixer();
	private volatile D3GraphWindow ui;

	public JavaFXApplication() {
		super();
		app = this;
	}

	@Override public void init() throws Exception {
		fixer.start();
		super.init();
	}

	@Override public void start(Stage stage) throws Exception {
		ui = new D3GraphWindow(stage);
		initialized.countDown();
		//fixer.interrupt(); // from here on it doesn't really matter, because most of JavaFX is already up and running
	}

	@Override public void stop() throws Exception {
		super.stop();
		fixer.interrupt();
	}

	public static void startLaunch() {
		if (initialized == null) {
			initialized = new CountDownLatch(1);
			new Thread() {
				@Override public void run() {
					Platform.setImplicitExit(false); // keep JavaFX alive
					Application.launch(JavaFXApplication.class);
				}
			}.start();
		} else {
			// already initialized, wait for show()
		}
	}

	public static D3GraphWindow show(final Project project) {
		try {
			initialized.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Platform.runLater(new Runnable() {
			@Override public void run() {
				app.ui.showUI(project);
			}
		});

		return app.ui;
	}

	public static void hide() {
		try {
			initialized.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Platform.runLater(new Runnable() {
			@Override public void run() {
				app.ui.closeUI();
			}
		});
	}

	/**
	 * @see <a href="http://stackoverflow.com/a/31100941/253468">Background</a>
	 * @see com.sun.webpane.sg.prism.FXGraphicsManager com.sun.webpane.platform.graphics.WCGPerfMeter
	 */
	private static class GradleJULFixer extends Thread {
		private static final boolean DEBUG = false;
		private static final String MISCHIEF_LOGGER = com.sun.webpane.sg.prism.WCGraphicsPrismContext.class.getName();
		public GradleJULFixer() {
			super(GradleJULFixer.class.getSimpleName());
			log("creating");
			setDaemon(true);
		}

		@Override public void run() {
			try {
				//noinspection InfiniteLoopStatement Thread should be interrupted when fixing is not needed any more
				while (true) {
					if (!isFixed()) {
						fix();
					}
					Thread.sleep(50); // yield, but also listen for interrupts
				}
			} catch (InterruptedException ex) {
				log("interrupted");
				Thread.currentThread().interrupt();
			}
		}

		public static boolean isFixed() {
			return !enabled(MISCHIEF_LOGGER)
					&& !enabled("com.sun.webpane.perf")
					&& !enabled("com.sun.webpane.perf.WCFontPerfLogger")
					&& !enabled("com.sun.webpane.perf.WCGraphicsPerfLogger")
					&& !enabled("com.sun.webpane.perf.Locks")
					&& !enabled("com.sun.webpane.perf.XXX")
					;
		}

		public static void fix() {
			log("fixing");
			disable(MISCHIEF_LOGGER);
			disable("com.sun.webpane.perf"); // should disable all children as well
			//running.set(false); // once fixed, stop running, except it can go wrong 2-3 times
		}

		@Override public void start() {
			log("starting");
			fix(); // fix quickly
			super.start();
		}

		private static boolean enabled(String name) {
			return java.util.logging.Logger.getLogger(name).isLoggable(java.util.logging.Level.FINE);
		}
		private static void disable(String name) {
			java.util.logging.Logger.getLogger(name).setLevel(java.util.logging.Level.OFF);
		}

		private static void log(String x) {
			if (DEBUG) {
				System.out.println("--------------------------> " + x);
			}
		}
	}
}
