@file:JvmName("Utils")

package net.twisterrob.gradle.common

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.AndroidVariantTask
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.build.gradle.internal.tasks.NonIncrementalGlobalTask
import com.android.build.gradle.internal.tasks.VariantAwareTask
import com.android.build.gradle.tasks.LintBaseTask
import com.android.build.gradle.tasks.LintFixTask
import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import com.android.builder.core.BuilderConstants
import org.gradle.api.DefaultTask
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

// TODO find globalScope.reportsDir and task.isFatalOnly
private val LintBaseTask.reportsDir get() = project.buildDir.resolve("reports")

@Suppress("unused")
private val LintBaseTask.isFatalOnly
	get() = false

/**
 * Workaround for 3.2 vs 3.3+: [LintBaseTask] extended [AndroidVariantTask] in 3.2, but not in 3.3+.
 * The `variantName` property was moved from [AndroidVariantTask] base class to [LintPerVariantTask].
 * Due to Kotlin limitations, cannot polyfill `variantName` (extension methods are compile time bound),
 * so introducing a separate property is a good compromise.
 * This only calls [LintPerVariantTask.variantName] when it actually exists.
 * 3.1.4, 3.2.1:
 *  * [AndroidVariantTask.variantName] : [DefaultTask]
 *  * [LintPerVariantTask] : [LintBaseTask]
 *  * [LintGlobalTask] : [LintBaseTask]
 *  * [LintFixTask] (new in 3.2.0) : [LintBaseTask]
 *  * [LintBaseTask] : `com.android.build.gradle.internal.tasks.AndroidBuilderTask`
 *  * `AndroidBuilderTask` : [AndroidVariantTask]
 * 3.3.3, 3.6.4, 4.0.0, 4.1.0:
 *  * [AndroidVariantTask] : [DefaultTask], [VariantAwareTask.variantName]
 *  * [LintPerVariantTask] : [LintBaseTask], [VariantAwareTask.variantName]
 *  * [LintGlobalTask] : [LintBaseTask]
 *  * [LintFixTask] : [LintBaseTask]
 *  * [LintBaseTask] : [DefaultTask]
 * 4.2.0:
 *  * [AndroidVariantTask] : [BaseTask], [VariantAwareTask.variantName]
 *  * [LintPerVariantTask] : [LintBaseTask], [VariantAwareTask.variantName]
 *  * [LintGlobalTask] : [LintBaseTask]
 *  * [LintFixTask] : [LintBaseTask]
 *  * [LintBaseTask] : [NonIncrementalGlobalTask]
 *  * [NonIncrementalGlobalTask] : [BaseTask], [VariantAwareTask.variantName] = ""
 *  * [BaseTask] : [DefaultTask]
 */
val LintBaseTask.androidVariantName: String?
	get() = when {
		this is LintGlobalTask ->
			this.variantInputMap.keys.singleOrNull()
		AGPVersions.v33x < AGPVersions.CLASSPATH && @Suppress("USELESS_IS_CHECK") (this is VariantAwareTask) ->
			// USELESS_IS_CHECK: Need to check for interface explicitly,
			// because before 4.2.0 LintGlobalTask/LintFixTask didn't implement the interface.
			// Force compile time binding to the interface, because a super of LintBaseTask may override the property.
			(this as VariantAwareTask).variantName
		AGPVersions.CLASSPATH < AGPVersions.v33x && this is AndroidVariantTask ->
			@Suppress("CAST_NEVER_SUCCEEDS") // Historical binding to inherited property.
			(this as AndroidVariantTask).variantName
		this is LintGlobalTask ->
			null
		AGPVersions.v32x < AGPVersions.CLASSPATH && this is LintFixTask ->
			null
		else ->
			null
	}

private val LintGlobalTask.variantInputMap: Map<String, *>
	@Suppress("UNCHECKED_CAST")
	get() = LintGlobalTask::class.java.getDeclaredField("variantInputMap")
		.apply { isAccessible = true }
		.get(this) as Map<String, *>

val LintBaseTask.xmlOutput: File
	get() = lintOptions.xmlOutput ?: LintOptions_createOutputPath(
		project, androidVariantName, SdkConstants.DOT_XML, reportsDir, isFatalOnly
	)

val LintBaseTask.htmlOutput: File
	get() = lintOptions.htmlOutput ?: LintOptions_createOutputPath(
		project, androidVariantName, ".html", reportsDir, isFatalOnly
	)

// lint-gradle:26.1.0: com.android.tools.lint.gradle.SyncOptions.createOutputPath
// lint-gradle:27.2.1: com.android.tools.lint.gradle.SyncOptionsKt.createOutputPath
@Suppress("FunctionName")
fun LintOptions_createOutputPath(
	project: Project, variantName: String?, extension: String, reportsDir: File?, fatalOnly: Boolean
): File {
	val base = buildString {
		append("lint-results")
		if (!variantName.isNullOrBlank()) {
			append("-")
			append(variantName)
		}

		if (fatalOnly) {
			append("-fatal")
		}

		append(extension)
	}
	return when {
		reportsDir != null -> File(reportsDir, base)
		else -> File(project.buildDir, BuilderConstants.FD_REPORTS + File.separator + base)
	}
}
