package net.twisterrob.gradle.graph.vis.d3

import net.twisterrob.gradle.graph.logger

private val LOG = logger<GraphWindow>()

/**
 * See [Background](http://stackoverflow.com/a/31100941/253468).
 * @see com.sun.webpane.sg.prism.FXGraphicsManager com.sun.webpane.platform.graphics.WCGPerfMeter
 */
class GradleJULFixer : Thread(GradleJULFixer::class.java.name) {

	init {
		LOG.trace("creating")
		isDaemon = true
	}

	override fun start() {
		LOG.trace("starting")
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
				Thread.sleep(@Suppress("detekt.MagicNumber") 50) // yield, but also listen for interrupts
			}
		} catch (ex: InterruptedException) {
			LOG.trace("interrupted")
			Thread.currentThread().interrupt()
		}
	}

	companion object {

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
			LOG.trace("fixing")
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
	}
}
