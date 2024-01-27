package net.twisterrob.gradle.logging

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.Logger
import org.slf4j.event.Level

/**
 * Referenced methods are using `Array<T>` in Kotlin Functional Types, because `vararg T` is not representable.
 * This changes how we call this function (have to explicitly create the vararg array),
 * but the method reference will bind to the `vararg` overload anyway.
 */
class GradleLogLevelRedirectingLoggerTest {

	@MethodSource("getGRADLE_LEVELS")
	@ParameterizedTest fun `no redirect`(it: GradleLevel) {
		val delegate: Logger = mock()
		with(it) { whenever(delegate.isEnabled()).thenReturn(true) }

		val logger = GradleLogLevelRedirectingLogger(delegate) { it }
		with(it) { logger.logMessage("test") }

		with(it) { verify(delegate).logMessage("test") }
	}

	@MethodSource("getGRADLE_LEVELS")
	@ParameterizedTest fun `off as trace`(it: GradleLevel) {
		val delegate: Logger = mock()
		whenever(delegate.isTraceEnabled).thenReturn(true) // This never happens in Gradle.

		val logger = GradleLogLevelRedirectingLogger(delegate) { null }
		with(it) { logger.logMessage("test") }

		verify(delegate).isTraceEnabled
		verify(delegate).trace("test")
	}

	@TestFactory fun `constant redirect`(): List<DynamicNode> =
		SLF4J_LEVELS.map { input ->
			dynamicContainer("from ${input.level}",
				GRADLE_LEVELS.map { output ->
					dynamicContainer(
						"to ${output.level}",
						listOf(
							dynamicTest("with message") { withMessage(input, output) },
							dynamicTest("with throwable") { withThrowable(input, output) },
							dynamicTest("with 1 arg") { withOneArg(input, output) },
							dynamicTest("with 2 args") { withTwoArgs(input, output) },
							dynamicTest("with varargs") { withVararg(input, output) },
							dynamicTest("with varargs and throwable") { withVarargAndThrowable(input, output) },
						)
					)
				}
			)
		}

	private fun withMessage(input: Slf4jLevel, output: GradleLevel) {
		val delegate: Logger = mock()
		with(output) { whenever(delegate.isEnabled()).thenReturn(true) }

		val logger = GradleLogLevelRedirectingLogger(delegate) { output.level }
		with(input) { logger.logMessage("test") }

		with(output) { verify(delegate).logMessage("test") }
	}

	private fun withThrowable(input: Slf4jLevel, output: GradleLevel) {
		val delegate: Logger = mock()
		with(output) { whenever(delegate.isEnabled()).thenReturn(true) }

		val throwable = RuntimeException()

		val logger = GradleLogLevelRedirectingLogger(delegate) { output.level }
		with(input) { logger.logThrowable("test", throwable) }

		with(output) { verify(delegate).logThrowable("test", throwable) }
	}

	private fun withOneArg(input: Slf4jLevel, output: GradleLevel) {
		val delegate: Logger = mock()
		with(output) { whenever(delegate.isEnabled()).thenReturn(true) }

		val arg1 = 1

		val logger = GradleLogLevelRedirectingLogger(delegate) { output.level }
		with(input) { logger.logArg1("test", arg1) }

		with(output) { verify(delegate).logVarArg("test", arrayOf(arg1)) }
	}

	private fun withTwoArgs(input: Slf4jLevel, output: GradleLevel) {
		val delegate: Logger = mock()
		with(output) { whenever(delegate.isEnabled()).thenReturn(true) }

		val arg1 = 1
		val arg2 = "2"

		val logger = GradleLogLevelRedirectingLogger(delegate) { output.level }
		with(input) { logger.logArg2("test", arg1, arg2) }

		with(output) { verify(delegate).logVarArg("test", arrayOf(arg1, arg2)) }
	}

	private fun withVararg(input: Slf4jLevel, output: GradleLevel) {
		val delegate: Logger = mock()
		with(output) { whenever(delegate.isEnabled()).thenReturn(true) }

		val arg1 = 1
		val arg2 = "2"
		val arg3 = 3.0

		val logger = GradleLogLevelRedirectingLogger(delegate) { output.level }
		with(input) { logger.logVarArg("test", arrayOf(arg1, arg2, arg3)) }

		with(output) { verify(delegate).logVarArg("test", arrayOf(arg1, arg2, arg3)) }
	}

	private fun withVarargAndThrowable(input: Slf4jLevel, output: GradleLevel) {
		val delegate: Logger = mock()
		with(output) { whenever(delegate.isEnabled()).thenReturn(true) }

		val arg1 = 1
		val arg2 = "2"
		val arg3 = 3.0
		val throwable = RuntimeException()

		val logger = GradleLogLevelRedirectingLogger(delegate) { output.level }
		with(input) { logger.logVarArg("test", arrayOf(arg1, arg2, arg3, throwable)) }

		with(output) { verify(delegate).logVarArg("test", arrayOf(arg1, arg2, arg3, throwable)) }
	}

	companion object {

		@JvmStatic
		@Suppress("detekt.NamedArguments")
		val GRADLE_LEVELS: List<GradleLevel> =
			listOf(
				GradleLevel(null, Logger::isTraceEnabled, Logger::trace, Logger::trace, Logger::trace),
				GradleLevel(LogLevel.ERROR, Logger::isErrorEnabled, Logger::error, Logger::error, Logger::error),
				GradleLevel(LogLevel.WARN, Logger::isWarnEnabled, Logger::warn, Logger::warn, Logger::warn),
				GradleLevel(LogLevel.INFO, Logger::isInfoEnabled, Logger::info, Logger::info, Logger::info),
				GradleLevel(LogLevel.DEBUG, Logger::isDebugEnabled, Logger::debug, Logger::debug, Logger::debug),
				GradleLevel(
					LogLevel.LIFECYCLE,
					{ isInfoEnabled(Logging.LIFECYCLE) },
					{ m -> info(Logging.LIFECYCLE, m) },
					{ m, t -> info(Logging.LIFECYCLE, m, t) },
					{ m, a -> info(Logging.LIFECYCLE, m, *a) },
				),
				GradleLevel(
					LogLevel.QUIET,
					{ isInfoEnabled(Logging.QUIET) },
					{ m -> info(Logging.QUIET, m) },
					{ m, t -> info(Logging.QUIET, m, t) },
					{ m, a -> info(Logging.QUIET, m, *a) },
				)
			)

		@JvmStatic
		@Suppress("detekt.NamedArguments")
		val SLF4J_LEVELS: List<Slf4jLevel> =
			@Suppress("detekt.MaxLineLength") // Keep it simple.
			listOf(
				//@formatter:off
				Slf4jLevel(Level.ERROR, Logger::isErrorEnabled, Logger::error, Logger::error, Logger::error, Logger::error, Logger::error),
				Slf4jLevel(Level.WARN, Logger::isWarnEnabled, Logger::warn, Logger::warn, Logger::warn, Logger::warn, Logger::warn),
				Slf4jLevel(Level.INFO, Logger::isInfoEnabled, Logger::info, Logger::info, Logger::info, Logger::info, Logger::info),
				Slf4jLevel(Level.DEBUG, Logger::isDebugEnabled, Logger::debug, Logger::debug, Logger::debug, Logger::debug, Logger::debug),
				Slf4jLevel(Level.TRACE, Logger::isTraceEnabled, Logger::trace, Logger::trace, Logger::trace, Logger::trace, Logger::trace),
				//@formatter:on
			)

		@Suppress(
			"detekt.LongParameterList", // It's how many variants of log methods exists in SLF4J.
			"detekt.UseDataClass", // No need to use data class here, none of its methods would be used.
		)
		class Slf4jLevel(
			val level: Level,
			val isEnabled: Logger.() -> Boolean,
			val logMessage: Logger.(String) -> Unit,
			val logThrowable: Logger.(String, Throwable) -> Unit,
			val logArg1: Logger.(String, Any?) -> Unit,
			val logArg2: Logger.(String, Any?, Any?) -> Unit,
			/**
			 * @see GradleLogLevelRedirectingLoggerTest why array and not vararg.
			 */
			val logVarArg: Logger.(String, Array<Any?>) -> Unit,
		) {
			override fun toString(): String =
				level.toString()
		}

		@Suppress(
			"detekt.LongParameterList", // It's how many variants of log methods exists in SLF4J.
			"detekt.UseDataClass", // No need to use data class here, none of its methods would be used.
		)
		class GradleLevel(
			val level: LogLevel?,
			val isEnabled: Logger.() -> Boolean,
			val logMessage: Logger.(String) -> Unit,
			val logThrowable: Logger.(String, Throwable) -> Unit,
			/**
			 * A side effect of redirect is that `(String, Any?)` and `(String, Any?, Any?)`
			 * overloads will always delegate to (String, vararg Any?).
			 *
			 * @see GradleLogLevelRedirectingLoggerTest why array and not vararg.
			 */
			val logVarArg: Logger.(String, Array<Any?>) -> Unit,
		) {
			override fun toString(): String =
				level?.toString() ?: "TRACE"
		}
	}
}
