package net.twisterrob.gradle.graph.vis.d3;

/**
 * @see <a href="http://stackoverflow.com/a/31100941/253468">Background</a>
 * @see com.sun.webpane.sg.prism.FXGraphicsManager com.sun.webpane.platform.graphics.WCGPerfMeter
 */
public class GradleJULFixer extends Thread {
	private static final boolean DEBUG = false;
	private static final String MISCHIEF_LOGGER = "com.sun.webpane.sg.prism.WCGraphicsPrismContext";
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
		disable("com.sun.webpane"); // should disable all children as well
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
