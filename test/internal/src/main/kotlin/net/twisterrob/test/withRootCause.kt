package net.twisterrob.test

fun Throwable.withRootCause(cause: Throwable): Throwable {
	require(this !in this.causalChain.drop(1)) { "${this} should not be present in its own causal chain." }
	require(cause !in this.causalChain) { "${cause} should not be already present in causal chain of ${this}." }
	// Note: In the past (pre JUnit 5.10.0) there was a need to reflectively set AssertionFailedErrors.cause
	// because of https://github.com/ota4j-team/opentest4j/issues/5#issuecomment-940474063.
	this.rootCause.initCause(cause)
	return this
}

private val Throwable.rootCause: Throwable
	get() = causalChain.last()

private val Throwable.causalChain: Sequence<Throwable>
	get() = generateSequence(this) { it.cause }
