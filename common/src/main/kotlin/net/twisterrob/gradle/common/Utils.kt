@file:JvmName("Utils")

package net.twisterrob.gradle.common

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Task
import java.io.File
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.Collectors

typealias Variants = DomainObjectSet<out BaseVariant>

fun safeAdd(a: Int?, b: Int?): Int? =
	when {
		a != null && b != null -> a + b
		a != null && b == null -> a
		a == null && b != null -> b
		a == null && b == null -> null
		else -> throw InternalError("No other possibility")
	}

fun nullSafeSum(): Collector<Int?, *, Int?> =
	nullSafeSum(Function.identity())

fun <T> nullSafeSum(mapper: Function<T?, Int?>): Collector<T?, *, Int?> {
	return Collectors.reducing(null, mapper, BinaryOperator(::safeAdd))
}

fun File.listFilesInDirectory(filter: ((File) -> Boolean)? = null): Array<File> {
	val listFiles: Array<File>? =
		if (filter != null)
			this.listFiles(filter)
		else
			this.listFiles()

	return listFiles ?: error(
		"$this does not denote a directory or an error occurred" +
				"\nisDirectory=${this.isDirectory}, exists=${this.exists()}, canRead=${this.canRead()}"
	)
}

val Task.wasLaunchedOnly: Boolean
	get() = project.gradle.startParameter.taskNames == listOf(path)

val Task.wasLaunchedExplicitly: Boolean
	get() = path in project.gradle.startParameter.taskNames

