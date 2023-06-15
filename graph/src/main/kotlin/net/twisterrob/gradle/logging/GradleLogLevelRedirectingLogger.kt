package net.twisterrob.gradle.logging

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.AbstractLogger

/**
 * Null means no redirect, but it is represented by a call to [Logger.trace].
 */
typealias LogLevelRedirect = (LogLevel?) -> LogLevel?

/**
 * Note: also tried manual Logger interface implementation and creating a Proxy,
 * but they're not as viable as using [AbstractLogger].
 */
class GradleLogLevelRedirectingLogger(
	private val delegate: Logger,
	private val remap: LogLevelRedirect,
) : FullAbstractLogger() {

	override fun handleNormalizedIsEnabledCall(level: Level, marker: Marker?): Boolean =
		delegate.dispatchIsEnabled(remap(infer(level, marker)))

	override fun handleNormalizedLoggingCall(
		level: Level,
		marker: Marker?,
		message: String,
		arguments: Array<out Any?>?,
		throwable: Throwable?
	) {
		val remapedLevel = remap(infer(level, marker))
		delegate.dispatchLog(level = remapedLevel, message = message, arguments = arguments, throwable = throwable)
	}

	/**
	 * No idea what this is, but it's not used in AbstractLogger.
	 * See also https://github.com/qos-ch/slf4j/discussions/348.
	 */
	override fun getFullyQualifiedCallerName(): String? =
		null

	companion object {
		/**
		 * Mimic what [org.gradle.internal.logging.slf4j.BuildOperationAwareLogger] does.
		 *
		 * @see org.gradle.internal.logging.slf4j.BuildOperationAwareLogger.toLogLevel
		 */
		private fun infer(level: Level, marker: Marker?): LogLevel? =
			when (level) {
				Level.ERROR -> LogLevel.ERROR
				Level.WARN -> LogLevel.WARN
				Level.INFO -> when (marker) {
					Logging.QUIET -> LogLevel.QUIET
					Logging.LIFECYCLE -> LogLevel.LIFECYCLE
					else -> LogLevel.INFO
				}
				Level.DEBUG -> LogLevel.DEBUG
				// Never logged in BuildOperationAwareLogger.
				Level.TRACE -> null
			}

		private fun Logger.dispatchIsEnabled(level: LogLevel?): Boolean =
			when (level) {
				null -> this.isTraceEnabled
				LogLevel.ERROR -> this.isErrorEnabled
				LogLevel.QUIET -> this.isInfoEnabled(Logging.QUIET)
				LogLevel.WARN -> this.isWarnEnabled
				LogLevel.LIFECYCLE -> this.isInfoEnabled(Logging.LIFECYCLE)
				LogLevel.INFO -> this.isInfoEnabled
				LogLevel.DEBUG -> this.isDebugEnabled
			}

		@Suppress("SpreadOperator", "CyclomaticComplexMethod", "CognitiveComplexMethod")
		private fun Logger.dispatchLog(
			level: LogLevel?,
			message: String,
			arguments: Array<out Any?>?,
			throwable: Throwable?
		) {
			@Suppress("UnnecessaryLet") // No other exhaustive way to do this.
			when (level) {
				null /* Level.TRACE */ -> when {
					arguments == null && throwable == null -> this.trace(message)
					arguments == null -> this.trace(message, throwable)
					throwable == null -> this.trace(message, *arguments)
					else -> this.trace(message, *arguments, throwable)
				}
				LogLevel.ERROR -> when {
					arguments == null && throwable == null -> this.error(message)
					arguments == null -> this.error(message, throwable)
					throwable == null -> this.error(message, *arguments)
					else -> this.error(message, *arguments, throwable)
				}
				LogLevel.QUIET -> when {
					arguments == null && throwable == null -> this.info(Logging.QUIET, message)
					arguments == null -> this.info(Logging.QUIET, message, throwable)
					throwable == null -> this.info(Logging.QUIET, message, *arguments)
					else -> this.info(Logging.QUIET, message, *arguments, throwable)
				}
				LogLevel.WARN -> when {
					arguments == null && throwable == null -> this.warn(message)
					arguments == null -> this.warn(message, throwable)
					throwable == null -> this.warn(message, *arguments)
					else -> this.warn(message, *arguments, throwable)
				}
				LogLevel.LIFECYCLE -> when {
					arguments == null && throwable == null -> this.info(Logging.LIFECYCLE, message)
					arguments == null -> this.info(Logging.LIFECYCLE, message, throwable)
					throwable == null -> this.info(Logging.LIFECYCLE, message, *arguments)
					else -> this.info(Logging.LIFECYCLE, message, *arguments, throwable)
				}
				LogLevel.INFO -> when {
					arguments == null && throwable == null -> this.info(message)
					arguments == null -> this.info(message, throwable)
					throwable == null -> this.info(message, *arguments)
					else -> this.info(message, *arguments, throwable)
				}
				LogLevel.DEBUG -> when {
					arguments == null && throwable == null -> this.debug(message)
					arguments == null -> this.debug(message, throwable)
					throwable == null -> this.debug(message, *arguments)
					else -> this.debug(message, *arguments, throwable)
				}
			}.let { /* Exhaustive. */ }
		}
	}
}
