package net.twisterrob.gradle.test

import org.gradle.api.internal.tasks.testing.worker.TestWorker
import org.gradle.internal.SystemProperties
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File

/**
 * Increased [org.gradle.api.tasks.testing.Test.maxParallelForks] yields trashing caches:
 * > Invalidating in-memory cache of .gradle-test-kit\caches\4.10.2\fileHashes\fileHashes.bin
 * is shown repeatedly and the parallel tests cannot start.
 * Changing the directory of the Gradle Test Kit helps to resolve this contention.
 *
 * Example usage:
 * ```build.gradle.kts
 * tasks.withType<Test>().configureEach {
 *     // See GradleTestKitDirRelocator for what enables this!
 *     maxParallelForks = 10
 *     // Limit memory usage of test forks. Gradle <5 allows 1/4th of total memory to be used, thus forbidding many forks.
 *     // Memory limit for the :plugin:test task running JUnit tests.
 *     // The Gradle builds use the default in DaemonParameters.
 *     maxHeapSize = "256M"
 * }
 * ```
 */
class GradleTestKitDirRelocator : BeforeEachCallback {

	override fun beforeEach(context: ExtensionContext) {
		setTestKitDir()
	}

	/**
	 * Mimic default behavior and trigger the non-default branch which reads from a system property
	 * in [org.gradle.testkit.runner.internal.DefaultGradleRunner.calculateTestKitDirProvider].
	 * See [org.gradle.api.tasks.testing.Test.executeTests] which sets the default.
	 *
	 * To reduce disk space it is recommended to reset the worker ID on every execution,
	 * see `resetWorkerIdToDefault.kt` how.
	 */
	private fun setTestKitDir() {
		val props = SystemProperties.getInstance()
		val dirName = ".gradle-test-kit-${props.userName}-${workerId}"
		@Suppress("DEPRECATION")
		testKitDir = File(props.javaIoTmpDir).resolve(dirName).absolutePath
	}

	companion object {

		private val workerId: String by systemProperty(TestWorker.WORKER_ID_SYS_PROPERTY)
		private var testKitDir: String by systemProperty(DefaultGradleRunner.TEST_KIT_DIR_SYS_PROP)
	}
}
