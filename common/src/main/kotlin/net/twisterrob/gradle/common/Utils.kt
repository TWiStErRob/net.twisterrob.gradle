@file:JvmName("Utils")

package net.twisterrob.gradle.common

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.LintBaseTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.Collectors

typealias Variants = DomainObjectSet<out BaseVariant>

fun safeAdd(a: Int?, b: Int?): Int? = when {
	a != null && b != null -> a + b
	a != null && b == null -> a
	a == null && b != null -> b
	a == null && b == null -> null
	else -> throw InternalError("No other possibility")
}

fun nullSafeSum(): Collector<Int?, *, Int?> = nullSafeSum(Function.identity())

fun <T> nullSafeSum(mapper: Function<T?, Int?>): Collector<T?, *, Int?> {
	return Collectors.reducing(null, mapper, BinaryOperator(::safeAdd))
}

val Task.wasExplicitlyLaunched: Boolean
	get() = project.gradle.startParameter.taskNames == listOf(path)

// TODO find globalScope.reportsDir and task.isFatalOnly
private val LintBaseTask.reportsDir get() = project.buildDir.resolve("reports")
@Suppress("unused")
private val LintBaseTask.isFatalOnly
	get() = false

val LintBaseTask.xmlOutput: File
	get() = lintOptions.xmlOutput ?: LintOptions_createOutputPath(
			project, variantName, SdkConstants.DOT_XML, reportsDir, isFatalOnly)

val LintBaseTask.htmlOutput: File
	get() = lintOptions.htmlOutput ?: LintOptions_createOutputPath(
			project, variantName, ".html", reportsDir, isFatalOnly)

// TODO figure out where to find com.android.tools.lint.gradle.SyncOptions#createOutputPath
@Suppress("FunctionName")
fun LintOptions_createOutputPath(
		project: Project, variantName: String?, extension: String, reportsDir: File?, fatalOnly: Boolean
): File {
	val base = StringBuilder().apply {
		append("lint-results")
		if (!variantName.isNullOrEmpty()) {
			append("-")
			append(variantName)
		}

		if (fatalOnly) {
			append("-fatal")
		}

		append(extension)
	}.toString()
	return when {
		reportsDir != null -> File(reportsDir, base)
		else -> File(project.buildDir, "reports" + File.separator + base)
	}
}
