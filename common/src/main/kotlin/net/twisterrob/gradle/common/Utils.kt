@file:JvmName("Utils")

package net.twisterrob.gradle.common

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.LintBaseTask
import com.android.build.gradle.tasks.LintFixTask
import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
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

val ANDROID_GRADLE_PLUGIN_VERSION: String
	get() {
		val versionClass: Class<*> =
			try {
				// Introduced in AGP 3.6.x.
				Class.forName("com.android.Version")
			} catch (ex: Throwable) {
				// Deprecated in AGP 3.6.x and removed in AGP 4.x.
				Class.forName("com.android.builder.model.Version")
			}
		return versionClass.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION").get(null) as String
	}

// TODO find globalScope.reportsDir and task.isFatalOnly
private val LintBaseTask.reportsDir get() = project.buildDir.resolve("reports")

@Suppress("unused")
private val LintBaseTask.isFatalOnly
	get() = false

/**
 * Workaround for 3.2 vs 3.3: [com.android.build.gradle.tasks.LintBaseTask]
 * extended [com.android.build.gradle.internal.tasks.AndroidVariantTask] in 3.2,
 * but not in 3.3 and the `variantName` property was moved to [LintPerVariantTask].
 * Due to Kotlin limitations, cannot polyfill `variantName` (extension methods are compile time bound),
 * so introducing a separate property is a good compromise.
 * This only calls [LintPerVariantTask.variantName] when it actually exists.
 */
val LintBaseTask.androidVariantName: String?
	get() = when (this) {
		is LintPerVariantTask -> variantName
		is LintGlobalTask -> null
		is LintFixTask -> null
		else -> null
	}

val LintBaseTask.xmlOutput: File
	get() = lintOptions.xmlOutput ?: LintOptions_createOutputPath(
		project, androidVariantName, SdkConstants.DOT_XML, reportsDir, isFatalOnly
	)

val LintBaseTask.htmlOutput: File
	get() = lintOptions.htmlOutput ?: LintOptions_createOutputPath(
		project, androidVariantName, ".html", reportsDir, isFatalOnly
	)

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
