package net.twisterrob.gradle.ext

import org.gradle.api.provider.Provider

fun <T, U, V, R> Provider<T>.zip(
	other1: Provider<U>,
	other2: Provider<V>,
	combiner: (value1: T, value2: U, value3: V) -> R
): Provider<R> =
	this.zip(other1.zip(other2, ::Pair)) { value1, value2Andvalue3 ->
		val (value2, value3) = value2Andvalue3
		combiner(value1, value2, value3)
	}
