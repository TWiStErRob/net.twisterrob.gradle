package net.twisterrob.gradle.common

import groovy.transform.CompileDynamic

import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.Collectors

final class Utils {

	private Utils() {
	}

	@CompileDynamic
	static <T> T safeAdd(T a, T b) {
		if (a != null && b != null) {
			return a + b
		} else if (a != null && b == null) {
			return a
		} else if (a == null && b != null) {
			return b
		} else /* (a == null && b == null) */ {
			return null
		}
	}

	static Collector<Integer, ?, Integer> nullSafeSum() {
		return nullSafeSum(Function.<Integer> identity())
	}

	/**
	 * @param mapper T is @Nullable
	 */
	static <T> Collector<T, ?, Integer> nullSafeSum(Function<T, Integer> mapper) {
		Collectors.reducing((Integer)null, mapper, Utils.&safeAdd as BinaryOperator<Integer>)
	}
}
