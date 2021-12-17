package net.twisterrob.test

import org.opentest4j.AssertionFailedError

fun Throwable.withRootCause(cause: Throwable): Throwable {
	require(cause !in generateSequence(this) { it.cause })
	val rootCause = generateSequence(this) { it.cause }.last()
	if (rootCause is AssertionFailedError) {
		// Workaround for https://github.com/ota4j-team/opentest4j/issues/5#issuecomment-940474063.
		// Can't use constructor, because would need to rebuild the whole chain:
		// AssertionFailedError(rootCause.message, rootCause.expected, rootCause.actual, cause)
		val causeField = Throwable::class.java.getDeclaredField("cause").apply { isAccessible = true }
		causeField.set(rootCause, rootCause)
	}
	rootCause.initCause(cause)
	return this
}
