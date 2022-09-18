@file:JvmName("Utils")

package net.twisterrob.gradle.common

import java.io.File
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.Collectors

fun safeAdd(a: Int?, b: Int?): Int? =
	@Suppress("KotlinConstantConditions") // Make it clearly explicit, sadly this is an inspection not exhaustiveness.
	when {
		a != null && b != null -> a + b
		a != null && b == null -> a
		a == null && b != null -> b
		a == null && b == null -> null
		else -> throw InternalError("No other possibility")
	}

fun nullSafeSum(): Collector<Int?, *, Int?> =
	nullSafeSum(Function.identity())

fun <T> nullSafeSum(mapper: Function<T?, Int?>): Collector<T?, *, Int?> =
	Collectors.reducing(null, mapper, BinaryOperator(::safeAdd))

fun File.listFilesInDirectory(filter: ((File) -> Boolean)? = null): Array<File> {
	val listFiles: Array<File>? =
		if (filter != null) {
			this.listFiles(filter)
		} else {
			this.listFiles()
		}

	return listFiles ?: error(
		"$this does not denote a directory or an error occurred" +
				"\nisDirectory=${this.isDirectory}, exists=${this.exists()}, canRead=${this.canRead()}"
	)
}
