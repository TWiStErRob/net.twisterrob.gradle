package net.twisterrob.gradle

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * @see disableLoggingFor
 */
class GradleUtilsTest_disableLoggingFor {

	@Test fun `logs do not reach if disabled before first use`() {
		val loggerName = "GradleUtilsTest.disableLoggingFor.beforeFirstUse"

		disableLoggingFor(loggerName)
		val log = LoggerFactory.getLogger(loggerName)

		assertThat(log, instanceOf(Class.forName(silentLogger)))
	}

	@Test fun `logs reach if disabled after first use`() {
		val loggerName = "GradleUtilsTest.disableLoggingFor.afterFirstUse"
		val original = LoggerFactory.getLogger(loggerName) // Create instance in the internal cache.
		assertThat(original, instanceOf(Class.forName(standardGradleLogger)))

		disableLoggingFor(loggerName) // This will overwrite it.
		val log = LoggerFactory.getLogger(loggerName)

		assertThat(log, instanceOf(Class.forName(silentLogger)))
	}

	companion object {
		private const val silentLogger =
			"org.gradle.internal.logging.slf4j.OutputEventListenerBackedLoggerContext\$NoOpLogger"
		private const val standardGradleLogger = "org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger"
	}
}
