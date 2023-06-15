package net.twisterrob.gradle.logging

import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.AbstractLogger

/**
 * An [AbstractLogger] that also routes all the trace functions to a central place.
 */
@Suppress("TooManyFunctions")
abstract class FullAbstractLogger : AbstractLogger() {
	abstract fun handleNormalizedIsEnabledCall(level: Level, marker: Marker?): Boolean

	//@formatter:off
	override fun isTraceEnabled(): Boolean = handleNormalizedIsEnabledCall(Level.TRACE, null)
	override fun isTraceEnabled(marker: Marker?): Boolean = handleNormalizedIsEnabledCall(Level.TRACE, marker)
	override fun isDebugEnabled(): Boolean = handleNormalizedIsEnabledCall(Level.DEBUG, null)
	override fun isDebugEnabled(marker: Marker?): Boolean = handleNormalizedIsEnabledCall(Level.DEBUG, marker)
	override fun isInfoEnabled(): Boolean = handleNormalizedIsEnabledCall(Level.INFO, null)
	override fun isInfoEnabled(marker: Marker?): Boolean = handleNormalizedIsEnabledCall(Level.INFO, marker)
	override fun isWarnEnabled(): Boolean = handleNormalizedIsEnabledCall(Level.WARN, null)
	override fun isWarnEnabled(marker: Marker?): Boolean = handleNormalizedIsEnabledCall(Level.WARN, marker)
	override fun isErrorEnabled(): Boolean = handleNormalizedIsEnabledCall(Level.ERROR, null)
	override fun isErrorEnabled(marker: Marker?): Boolean = handleNormalizedIsEnabledCall(Level.ERROR, marker)
	//@formatter:on
}
