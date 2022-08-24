package net.twisterrob.gradle.internal.lint

import com.android.SdkConstants
import com.android.build.gradle.internal.tasks.AndroidVariantTask
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.build.gradle.internal.tasks.NonIncrementalGlobalTask
import com.android.build.gradle.internal.tasks.VariantAwareTask
import com.android.build.gradle.tasks.LintBaseTask
import com.android.build.gradle.tasks.LintFixTask
import com.android.build.gradle.tasks.LintGlobalTask
import net.twisterrob.gradle.common.AGPVersions
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import java.io.File

// TODO find globalScope.net.twisterrob.gradle.internal.lint.getReportsDir and task.net.twisterrob.gradle.internal.lint.isFatalOnly
private val LintBaseTask.reportsDir
	get() = project.buildDir.resolve("reports")

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
		AGPVersions.v33x < AGPVersions.CLASSPATH && @Suppress("USELESS_IS_CHECK") (this is VariantAwareTask) ->
			// USELESS_IS_CHECK: Need to check for interface explicitly,
			// because before 4.2.0 LintGlobalTask/LintFixTask didn't implement the interface.
			// Force compile time binding to the interface, because a super of LintBaseTask may override the property.
			(this as VariantAwareTask).variantName
		AGPVersions.CLASSPATH < AGPVersions.v33x && @Suppress("USELESS_IS_CHECK") (this is AndroidVariantTask) ->
			// USELESS_IS_CHECK: Historical binding to inherited property.
			@Suppress("CAST_NEVER_SUCCEEDS")
			(this as AndroidVariantTask).variantName
		this is LintGlobalTask -> null
		AGPVersions.v32x < AGPVersions.CLASSPATH && this is LintFixTask -> null
		else -> null
	}

val LintBaseTask.xmlOutput: File
	get() = lintOptions.xmlOutput
		?: LintOptions_createOutputPath(project, androidVariantName, SdkConstants.DOT_XML, reportsDir, isFatalOnly)

val LintBaseTask.htmlOutput: File
	get() = lintOptions.htmlOutput
		?: LintOptions_createOutputPath(project, androidVariantName, ".html", reportsDir, isFatalOnly)

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
