package net.twisterrob.gradle.test

import net.twisterrob.gradle.systemProperty
import org.gradle.api.internal.tasks.testing.worker.TestWorker
import org.gradle.internal.SystemProperties
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File

/**
 * Increased [org.gradle.api.tasks.testing.Test.maxParallelForks] yields trashing caches:
 * > Invalidating in-memory cache of .gradle-test-kit\caches\4.10.2\fileHashes\fileHashes.bin
 * is shown repeatedly and the parallel tests cannot start.
 * Changing the directory of the Gradle Test Kit helps to resolve this contention.
 */
class GradleTestKitDirRelocator : TestRule {

	override fun apply(base: Statement, description: Description) = Statement {
		setTestKitDir()
		base.evaluate()
	}

	/**
	 * Mimic default behavior and trigger the non-default branch
	 * in [org.gradle.testkit.runner.internal.DefaultGradleRunner.calculateTestKitDirProvider].
	 * @see org.gradle.testkit.runner.internal.TempTestKitDirProvider
	 */
	private fun setTestKitDir() {
		val props = SystemProperties.getInstance()
		val dirName = ".gradle-test-kit-${props.userName}-${workerId}"
		testKitDir = File(props.javaIoTmpDir).resolve(dirName).absolutePath
	}

	companion object {
		private val workerId by systemProperty(TestWorker.WORKER_ID_SYS_PROPERTY)
		private var testKitDir by systemProperty(DefaultGradleRunner.TEST_KIT_DIR_SYS_PROP)
	}
}
