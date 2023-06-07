package net.twisterrob.gradle.graph.vis.d3.javafx;

import java.util.concurrent.*;

import javafx.application.*;
import javafx.stage.Stage;

import net.twisterrob.gradle.graph.vis.d3.GradleJULFixer;
import net.twisterrob.gradle.graph.vis.d3.javafx.JavaFXApplication.AbortableCountDownLatch.AbortedException;
import net.twisterrob.gradle.graph.vis.d3.javafx.Settings.WindowLocation;

public class JavaFXApplication extends Application {
	private static final boolean DEBUG = false;
	private static AbortableCountDownLatch initialized;
	private static volatile JavaFXApplication app;
	private static WindowLocation settings;

	private final GradleJULFixer fixer = new GradleJULFixer();
	private volatile GraphWindow ui;

	public JavaFXApplication() {
		super();
		app = this;
		log("ctor");
	}

	@Override public void init() throws Exception {
		log("init");
		fixer.start();
		super.init();
	}

	@Override public void start(Stage stage) throws Exception {
		log("start");
		settings.applyTo(stage);
		ui = new GraphWindow(stage);
		initialized.countDown();
		//fixer.interrupt(); // from here on it doesn't really matter, because most of JavaFX is already up and running
	}

	@Override public void stop() throws Exception {
		log("stop");
		super.stop();
		fixer.interrupt();
	}

	public static void startLaunch(WindowLocation settings) {
		log("startLaunch");
		if (initialized == null) {
			initialized = new AbortableCountDownLatch(1);
			JavaFXApplication.settings = settings;
			log("launching in background");
			new Thread() {
				@Override public void run() {
					log("launching");
					try {
						Platform.setImplicitExit(false); // keep JavaFX alive
						Application.launch(JavaFXApplication.class);
					} catch (RuntimeException ex) {
						if (ex.getCause() instanceof UnsatisfiedLinkError) {
							System.err.println("Sorry, JavaFX is clashing in Gradle daemon, "
									+ "try again after a `gradle --stop` or add `--no-daemon`\n"
									+ ex.getCause().toString());
						} else {
							ex.printStackTrace();
						}
						initialized.abort();
					}
				}
			}.start();
		} else {
			log("already initialized, wait for show()");
		}
	}

	public static GraphWindow show(final org.gradle.api.initialization.Settings project) {
		try {
			log("show, waiting");
			initialized.await();
			log("show, initialized");
			Platform.runLater(new Runnable() {
				@Override public void run() {
					log("show, showUI");
					app.ui.showUI(project);
				}
			});
			return app.ui;
		} catch (AbortedException ex) {
			//System.err.println("No JavaFX Application UI will be shown");
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
		}
		return null;
	}

	public static void hide() {
		try {
			log("hide, waiting");
			initialized.await();
			log("hide, initialized");
			Platform.runLater(new Runnable() {
				@Override public void run() {
					log("hide, closeUI");
					app.ui.closeUI();
				}
			});
		} catch (AbortedException ex) {
			//System.err.println("JavaFX Application UI cannot be hidden");
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	public static void log(String message) {
		if (DEBUG) {
			System.out.println(message);
		}
	}

	static class AbortableCountDownLatch extends CountDownLatch {
		protected boolean aborted = false;

		public AbortableCountDownLatch(int count) {
			super(count);
		}

		/**
		 * Unblocks all threads waiting on this latch and cause them to receive an {@link AbortedException}.
		 * If the latch has already counted all the way down, this method does nothing.
		 */
		public void abort() {
			aborted = true;
			while (getCount() > 0) {
				countDown();
			}
		}

		@SuppressWarnings("NullableProblems")
		@Override public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
			final boolean result = super.await(timeout, unit);
			checkAborted();
			return result;
		}

		@Override public void await() throws InterruptedException {
			super.await();
			checkAborted();
		}

		private void checkAborted() throws AbortedException {
			if (aborted) {
				throw new AbortedException();
			}
		}

		@groovy.transform.InheritConstructors
		public static class AbortedException extends InterruptedException {
		}
	}
}
