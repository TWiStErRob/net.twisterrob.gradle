package net.twisterrob.gradle.java

import net.twisterrob.gradle.android.hasAndroid
import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType

private const val DEFAULT_ENCODING = "UTF-8"

abstract class BaseJavaPlugin : BaseExposedPlugin() {

	abstract fun applyDefaultPlugin()

	override fun apply(target: Project) {
		super.apply(target)

		if (project.plugins.hasAndroid()) {
			@Suppress("DEPRECATION" /* AGP 9.0 */)
			val android = project.extensions["android"] as com.android.build.gradle.BaseExtension
			with(android.compileOptions) {
				encoding = DEFAULT_ENCODING
			}
		} else {
			applyDefaultPlugin()
		}

		project.tasks.withType<JavaCompile>().configureEach { task ->
			task.options.encoding = DEFAULT_ENCODING
			val isTestTask = task.name.contains("Test")
			task.configureCompilerArgs(isTestTask)
		}
	}
}

private fun JavaCompile.configureCompilerArgs(isTestTask: Boolean) {
	if (!isTestTask) {
		this.options.compilerArgs.add("-Xlint:unchecked")
		this.options.compilerArgs.add("-Xlint:deprecation")
	}
	this.doFirst { this.removeDuplicateCompilerArgs() }
}

/**
 * Removes duplicate `-Xlint:opt` parameters from a `javac` task.
 * If something is both disabled and enabled, make the disable win.
 *
 * Examples:
 *  * given: `-Xlint:unchecked -Xlint:-deprecation -Xlint:deprecation`,
 *    result: `-Xlint:unchecked -Xlint:-deprecation`
 *  * given: `-Xlint:unchecked -Xlint:deprecation -Xlint:-deprecation`,
 *    result: `-Xlint:unchecked -Xlint:-deprecation`
 *
 * Usage:
 * ```
 * tasks.withType(JavaCompile).configureEach { doFirst { removeDuplicateCompilerArgs(it) } }
 * ```
 *
 * Since the suggested usage is [org.gradle.api.Task.doFirst],
 * it doesn't matter if it's before or after the relevant `options.compilerArgs += [ ... ]` setup.
 */
@Suppress("detekt.StringLiteralDuplication")
private fun JavaCompile.removeDuplicateCompilerArgs() {
	logger.debug("${this} (input): ${options.compilerArgs}")
	val duplicates = options.compilerArgs
		.filter { it.startsWith("-Xlint:-") }
		.map { "-Xlint:${it.substring("-Xlint:-".length)}" }
	options.compilerArgs.removeAll(duplicates)
	logger.debug("${this} (filtered): ${options.compilerArgs}")
}

/**
 * Removes duplicate `-Xlint:opt` parameters from a `javac` task.
 * If something is both disabled and enabled, the last one wins.
 *
 * Examples:
 *  * given: `-Xlint:unchecked -Xlint:-deprecation -Xlint:deprecation`,
 *    result: `-Xlint:unchecked -Xlint:deprecation`
 *  * given: `-Xlint:unchecked -Xlint:deprecation -Xlint:-deprecation`,
 *    result: `-Xlint:unchecked -Xlint:-deprecation`
 *
 * Usage:
 * ```
 * tasks.withType(JavaCompile).configureEach { doFirst { removeDuplicateCompilerArgs2(it) } }
 * ```
 *
 * Since the suggested usage is [org.gradle.api.Task.doFirst],
 * it doesn't matter if it's before or after the relevant `options.compilerArgs += [ ... ]` setup.
 */
@Suppress("detekt.StringLiteralDuplication", "detekt.UnusedPrivateMember")
private fun JavaCompile.removeDuplicateCompilerArgs2() {
	logger.debug("${this} (input): ${options.compilerArgs}")
	fun xlintName(arg: String): String? =
		when {
			arg.startsWith("-Xlint:-") -> arg.substring("-Xlint:-".length)
			arg.startsWith("-Xlint:") -> arg.substring("-Xlint:".length)
			else -> null
		}

	fun xlintEnabled(arg: String): Boolean? =
		when {
			arg.startsWith("-Xlint:-") -> false
			arg.startsWith("-Xlint:") -> true
			else -> null
		}

	val optionMap = options.compilerArgs
		.filter { xlintName(it) != null }
		.associate { xlintName(it) to xlintEnabled(it) }
	logger.debug("${this} (deduced): $optionMap")
	options.compilerArgs = options.compilerArgs.filter { optionMap[xlintName(it)] == xlintEnabled(it) }
	logger.debug("${this} (filtered): ${options.compilerArgs}")
}
