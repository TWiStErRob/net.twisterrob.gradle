package net.twisterrob.gradle.graph.vis.d3.javafx

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import net.twisterrob.gradle.graph.vis.d3.GradleJULFixer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

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
		settings!!.applyTo(stage)
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

		protected var aborted: Boolean = false

		/**
		 * Unblocks all threads waiting on this latch and cause them to receive an [AbortedException].
		 * If the latch has already counted all the way down, this method does nothing.
		 */
		fun abort() {
			aborted = true
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
			if (aborted) {
				throw AbortedException()
			}
		}

		class AbortedException : InterruptedException()
	}

	companion object {

		private const val DEBUG: Boolean = false
		private var initialized: AbortableCountDownLatch? = null

		@Volatile
		private lateinit var app: JavaFXApplication
		private var settings: Settings.WindowLocation? = null
		internal fun startLaunch(settings: Settings.WindowLocation) {
			log("startLaunch")
			if (initialized == null) {
				initialized = AbortableCountDownLatch(1)
				JavaFXApplication.settings = settings
				log("launching in background")
				thread {
					log("launching")
					try {
						Platform.setImplicitExit(false) // Keep JavaFX alive.
						launch(JavaFXApplication::class.java)
					} catch (ex: RuntimeException) {
						if (ex.cause is UnsatisfiedLinkError) {
							System.err.println(
								("Sorry, JavaFX is clashing in Gradle daemon, "
										+ "try again after a `gradle --stop` or add `--no-daemon`\n"
										+ ex.cause.toString())
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

		fun show(project: org.gradle.api.initialization.Settings): GraphWindow? {
			try {
				log("show, waiting")
				initialized!!.await()
				log("show, initialized")
				Platform.runLater {
					log("show, showUI")
					app.ui!!.showUI(project)
				}
				return app.ui
			} catch (ex: AbortableCountDownLatch.AbortedException) {
				//System.err.println("No JavaFX Application UI will be shown")
			} catch (ex: InterruptedException) {
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
			} catch (ex: AbortableCountDownLatch.AbortedException) {
				//System.err.println("JavaFX Application UI cannot be hidden")
			} catch (ex: InterruptedException) {
				ex.printStackTrace()
				Thread.currentThread().interrupt()
			}
		}

		fun log(message: String?) {
			if (DEBUG) {
				println(message)
			}
		}
	}
}
