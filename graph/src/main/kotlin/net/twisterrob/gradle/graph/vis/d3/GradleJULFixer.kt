package net.twisterrob.gradle.graph.vis.d3

/**
 * See [Background](http://stackoverflow.com/a/31100941/253468).
 * @see com.sun.webpane.sg.prism.FXGraphicsManager com.sun.webpane.platform.graphics.WCGPerfMeter
 */
class GradleJULFixer : Thread(GradleJULFixer::class.java.name) {

	init {
		log("creating")
		isDaemon = true
	}

	override fun start() {
		log("starting")
		fix() // fix quickly
		super.start()
	}

	@Suppress("RemoveRedundantQualifierName")
	override fun run() {
		try {
			//noinspection InfiniteLoopStatement Thread should be interrupted when fixing is not needed any more
			while (true) {
				if (!isFixed) {
					fix()
				}
				Thread.sleep(@Suppress("MagicNumber") 50) // yield, but also listen for interrupts
			}
		} catch (ex: InterruptedException) {
			log("interrupted")
			Thread.currentThread().interrupt()
		}
	}

	companion object {

		private const val DEBUG = false

		@Suppress("SimplifyBooleanWithConstants")
		val isFixed: Boolean
			get() = true
					&& !enabled("com.sun.webpane.sg.prism.WCGraphicsPrismContext")
					&& !enabled("com.sun.webpane.perf")
					&& !enabled("com.sun.webpane.perf.WCFontPerfLogger")
					&& !enabled("com.sun.webpane.perf.WCGraphicsPerfLogger")
					&& !enabled("com.sun.webpane.perf.Locks")
					&& !enabled("com.sun.webpane.perf.XXX")

		@JvmStatic
		fun fix() {
			log("fixing")
			disable("com.sun.webpane.sg.prism.WCGraphicsPrismContext")
			disable("com.sun.webpane.perf") // should disable all children as well
			disable("com.sun.webpane") // should disable all children as well
			//running.set(false); // once fixed, stop running, except it can go wrong 2-3 times
		}

		private fun enabled(name: String): Boolean =
			java.util.logging.Logger.getLogger(name).isLoggable(java.util.logging.Level.FINE)

		private fun disable(name: String) {
			java.util.logging.Logger.getLogger(name).level = java.util.logging.Level.OFF
		}

		private fun log(x: String) {
			if (DEBUG) {
				@Suppress("ForbiddenMethodCall") // TODO logging
				println("--------------------------> $x")
			}
		}
	}
}
