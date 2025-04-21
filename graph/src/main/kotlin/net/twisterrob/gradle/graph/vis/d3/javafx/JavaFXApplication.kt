package net.twisterrob.gradle.graph.vis.d3.javafx

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import net.twisterrob.gradle.graph.logger
import net.twisterrob.gradle.graph.vis.d3.GradleJULFixer
import org.gradle.api.initialization.Settings
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private val LOG = logger<JavaFXApplication>()

@Suppress("UnsafeCallOnNullableType") // TODO rewrite this class without static/nullable/lateinit state.
class JavaFXApplication : Application() {

	private val fixer: GradleJULFixer = GradleJULFixer()

	@Volatile
	private var ui: GraphWindow? = null

	init {
		app = this
		LOG.trace("ctor")
	}

	@Throws(Exception::class)
	override fun init() {
		LOG.trace("init")
		fixer.start()
		super.init()
	}

	@Throws(Exception::class)
	override fun start(stage: Stage) {
		LOG.trace("start")
		options!!.applyTo(stage)
		ui = GraphWindow(stage)
		initialized!!.countDown()
		//fixer.interrupt(); // from here on it doesn't really matter, because most of JavaFX is already up and running
	}

	@Throws(Exception::class)
	override fun stop() {
		LOG.trace("stop")
		super.stop()
		fixer.interrupt()
	}

	internal class AbortableCountDownLatch(count: Int) : CountDownLatch(count) {

		@Volatile
		private var isAborted: Boolean = false

		/**
		 * Unblocks all threads waiting on this latch and cause them to receive an [AbortedException].
		 * If the latch has already counted all the way down, this method does nothing.
		 */
		fun abort() {
			isAborted = true
			while (count > 0) {
				countDown()
			}
		}

		@Throws(InterruptedException::class)
		override fun await(timeout: Long, unit: TimeUnit): Boolean =
			super.await(timeout, unit).also { checkAborted() }

		@Throws(InterruptedException::class)
		override fun await() {
			super.await()
			checkAborted()
		}

		@Throws(AbortedException::class)
		private fun checkAborted() {
			if (isAborted) {
				throw AbortedException()
			}
		}

		class AbortedException : InterruptedException()
	}

	companion object {

		@Suppress("detekt.LateinitUsage") // TODO convert to a proper object/singleton/etc.
		@Volatile
		private lateinit var app: JavaFXApplication

		private var initialized: AbortableCountDownLatch? = null

		private var options: Options.WindowLocation? = null

		internal fun startLaunch(options: Options.WindowLocation) {
			LOG.trace("startLaunch")
			if (initialized == null) {
				initialized = AbortableCountDownLatch(1)
				JavaFXApplication.options = options
				LOG.trace("launching in background")
				thread(name = "Launcher for ${JavaFXApplication::class.java.name}") {
					LOG.trace("launching")
					try {
						Platform.setImplicitExit(false) // Keep JavaFX alive.
						launch(JavaFXApplication::class.java)
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
						initialized!!.abort()
					}
				}
			} else {
				LOG.trace("already initialized, wait for show()")
			}
		}

		fun show(settings: Settings): GraphWindow? {
			try {
				LOG.trace("show, waiting")
				initialized!!.await()
				LOG.trace("show, initialized")
				Platform.runLater {
					LOG.trace("show, showUI")
					app.ui!!.showUI(settings)
				}
				return app.ui
			} catch (ex: AbortableCountDownLatch.AbortedException) {
				LOG.warn("No JavaFX Application UI will be shown", ex)
			} catch (ex: InterruptedException) {
				LOG.warn("show, interrupted", ex)
				Thread.currentThread().interrupt()
			}
			return null
		}

		fun hide() {
			try {
				LOG.trace("hide, waiting")
				initialized!!.await()
				LOG.trace("hide, initialized")
				Platform.runLater {
					LOG.trace("hide, closeUI")
					app.ui!!.closeUI()
				}
			} catch (ex: AbortableCountDownLatch.AbortedException) {
				LOG.warn("JavaFX Application UI cannot be hidden", ex)
			} catch (ex: InterruptedException) {
				LOG.warn("hide, interrupted", ex)
				Thread.currentThread().interrupt()
			}
		}
	}
}
