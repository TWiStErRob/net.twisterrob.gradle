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

fun wasExplicitlyLaunched(task: Task): Boolean =
		task.project.gradle.startParameter.taskNames == listOf(task.path)

fun getXmlOutput(task: LintBaseTask): File {
	val xmlOutput = task.lintOptions.xmlOutput
	if (xmlOutput != null) {
		return xmlOutput
	}

	return LintOptions_createOutputPath(
			task.project,
			task.variantName,
			SdkConstants.DOT_XML,
			task.reportsDir,
			task.isFatalOnly
	)
}

@Suppress("FunctionName")
private fun LintOptions_createOutputPath(
		project: Project?, variantName: String?, extension: String, reportsDir: File?, fatalOnly: Boolean): File {
	val createOutputPath = com.android.build.gradle.internal.dsl.LintOptions::class.java.getDeclaredMethod(
			"createOutputPath",
			Project::class.java,
			String::class.java,
			String::class.java,
			File::class.java,
			Boolean::class.javaPrimitiveType
	)
	createOutputPath.isAccessible = true
	return createOutputPath.invoke(null, project, variantName, extension, reportsDir, fatalOnly) as File
}
