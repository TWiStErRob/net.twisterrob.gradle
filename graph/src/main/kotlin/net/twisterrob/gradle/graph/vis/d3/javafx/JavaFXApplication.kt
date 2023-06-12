package net.twisterrob.gradle.graph.vis.d3.javafx

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import net.twisterrob.gradle.graph.vis.d3.Debug
import net.twisterrob.gradle.graph.vis.d3.GradleJULFixer
import org.gradle.api.initialization.Settings
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Suppress("UnsafeCallOnNullableType") // TODO rewrite this class without static/nullable/lateinit state.
class JavaFXApplication : Application() {

	private val fixer: GradleJULFixer = GradleJULFixer()

	@Volatile
	private var ui: GraphWindow? = null

	init {
		app = this
		log("ctor")
	}

	@Throws(Exception::class)
	override fun init() {
		log("init")
		fixer.start()
		super.init()
	}

	@Throws(Exception::class)
	override fun start(stage: Stage) {
		log("start")
		options!!.applyTo(stage)
		ui = GraphWindow(stage)
		initialized!!.countDown()
		//fixer.interrupt(); // from here on it doesn't really matter, because most of JavaFX is already up and running
	}

	@Throws(Exception::class)
	override fun stop() {
		log("stop")
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

		@Suppress("LateinitUsage") // TODO convert to a proper object/singleton/etc.
		@Volatile
		private lateinit var app: JavaFXApplication

		private var initialized: AbortableCountDownLatch? = null

		private var options: Options.WindowLocation? = null

		internal fun startLaunch(options: Options.WindowLocation) {
			log("startLaunch")
			if (initialized == null) {
				initialized = AbortableCountDownLatch(1)
				JavaFXApplication.options = options
				log("launching in background")
				thread(name = "Launcher for ${JavaFXApplication::class.java.name}") {
					log("launching")
					try {
						Platform.setImplicitExit(false) // Keep JavaFX alive.
						launch(JavaFXApplication::class.java)
					} catch (@Suppress("TooGenericExceptionCaught") ex: RuntimeException) {
						// TooGenericExceptionCaught: that's what JavaFX declares.
						if (ex.cause is UnsatisfiedLinkError) {
							System.err.println( // TODO logging
								"Sorry, JavaFX is clashing in Gradle daemon, "
										+ "try again after a `gradle --stop` or add `--no-daemon`\n"
										+ ex.cause.toString()
							)
						} else {
							ex.printStackTrace()
						}
						initialized!!.abort()
					}
				}
			} else {
				log("already initialized, wait for show()")
			}
		}

		fun show(settings: Settings): GraphWindow? {
			try {
				log("show, waiting")
				initialized!!.await()
				log("show, initialized")
				Platform.runLater {
					log("show, showUI")
					app.ui!!.showUI(settings)
				}
				return app.ui
			} catch (ignore: AbortableCountDownLatch.AbortedException) {
				// TODO logging
				//System.err.println("No JavaFX Application UI will be shown")
			} catch (ex: InterruptedException) {
				@Suppress("PrintStackTrace") // TODO logging
				ex.printStackTrace()
				Thread.currentThread().interrupt()
			}
			return null
		}

		fun hide() {
			try {
				log("hide, waiting")
				initialized!!.await()
				log("hide, initialized")
				Platform.runLater {
					log("hide, closeUI")
					app.ui!!.closeUI()
				}
			} catch (ignore: AbortableCountDownLatch.AbortedException) {
				// TODO logging
				//System.err.println("JavaFX Application UI cannot be hidden")
			} catch (ex: InterruptedException) {
				@Suppress("PrintStackTrace") // TODO logging
				ex.printStackTrace()
				Thread.currentThread().interrupt()
			}
		}

		fun log(message: String?) {
			if (Debug.JavaFx) {
				@Suppress("ForbiddenMethodCall") // TODO logging
				println(message)
			}
		}
	}
}
