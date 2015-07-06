package net.twisterrob.gradle.graph.javafx;

import java.util.concurrent.CountDownLatch;

import org.gradle.api.Project;

import javafx.application.*;
import javafx.stage.Stage;

public class HelloJavaFX2 extends Application {
	private static CountDownLatch initialized;
	private static volatile HelloJavaFX2 app;

	private final GradleJULFixer fixer = new GradleJULFixer();
	private volatile GraphWindow ui;

	public HelloJavaFX2() {
		super();
		app = this;
	}

	@Override public void init() throws Exception {
		fixer.start();
		super.init();
	}

	@Override public void start(Stage stage) throws Exception {
		ui = new GraphWindow(stage);
		initialized.countDown();
		//fixer.interrupt(); // from here on it doesn't really matter, because most of JavaFX is already up and running
	}

	@Override public void stop() throws Exception {
		super.stop();
		fixer.interrupt();
	}

	public static void startLaunch() {
		HelloJavaFX2.initialized = new CountDownLatch(1);
		if (app == null) {
			new Thread() {
				@Override public void run() {
					Platform.setImplicitExit(false); // keep JavaFX alive
					Application.launch(HelloJavaFX2.class);
				}
			}.start();
		} else {
			// already initialized, wait for show()
		}
	}

	public static GraphWindow show(final Project project) {
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

	/** @see http://stackoverflow.com/a/31100941/253468 com.sun.webpane.platform.graphics.WCGPerfMeter */
	private static class GradleJULFixer extends Thread {
		private static final boolean DEBUG = false;
		private static final String MISCHIEF_LOGGER = com.sun.webpane.sg.prism.WCGraphicsPrismContext.class.getName();
		public GradleJULFixer() {
			super(GradleJULFixer.class.getSimpleName());
			log("creating");
			setDaemon(true);
		}

		public static boolean isFixed() {
			return !java.util.logging.Logger.getLogger(MISCHIEF_LOGGER).isLoggable(java.util.logging.Level.FINE);
		}
		public static void fix() {
			log("fixing");
			java.util.logging.Logger.getLogger("com.sun.webpane.perf").setLevel(java.util.logging.Level.OFF);
			java.util.logging.Logger.getLogger(MISCHIEF_LOGGER).setLevel(java.util.logging.Level.OFF);
			//running.set(false); // once fixed, stop running, except it can go wrong 2-3 times
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

		@Override public synchronized void start() {
			log("starting");
			fix(); // fix quickly
			super.start();
		}

		private static void log(String x) {
			if (DEBUG) {
				System.out.println("--------------------------> " + x);
			}
		}
	}
}
