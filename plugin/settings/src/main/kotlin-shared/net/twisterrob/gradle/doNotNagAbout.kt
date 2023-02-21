@file:JvmMultifileClass
@file:JvmName("GradleUtils")

package net.twisterrob.gradle

import org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler
import org.gradle.util.GradleVersion
import java.util.regex.Pattern

/**
 * Surgically ignoring messages coming from Gradle deprecation system.
 *
 * Suppressing messages like this will prevent actual executions from triggering
 * stack traces and warnings, which means that even with some warnings,
 * it's possible to use `org.gradle.warning.mode=fail`.
 *
 * Be mindful of a few things:
 *  - This is a hack, it's not guaranteed to work in future Gradle versions.
 *  - The messages may contain version numbers, which will change as you upgrade Gradle or plugins,
 *    so either suppress dynamically (example below), or be prepared to review the messages every upgrade.
 *  - Gradle 8 internals have changed,
 *    at that point the messages may contain the stack trace of the occurrence
 *    (depending on a flag, see `doNotNagAbout(String, String)` for more info).
 *    This gives us the ability to specifically ignore an instance of a type of message.
 *    This is useful if multiple plugins are behind on fixing the deprecations.
 *  - `fail` was not a valid option for `--warning-mode` before Gradle 5.6.0.
 *  - In case it's not working, enable diagnostic logging by putting this before the first `doNotNagAbout` call:
 *    ```
 *    System.setProperty("net.twisterrob.gradle.nagging.diagnostics", "true")
 *    ```
 *
 * ### Example 1 - suppressing generic deprecation.
 * Realistic usage example with Gradle 7.5.1 and Android Gradle Plugin 7.2.2:
 * ```kotlin
 * // Ignore warning for https://issuetracker.google.com/issues/218478028 since Gradle 7.5,
 * // it's going to be fixed in AGP 7.3.
 * if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION < "7.3") {
 *   val gradleVersion: String = GradleVersion.current().version
 *   doNotNagAbout( // doNotNagAbout(String)
 *     "IncrementalTaskInputs has been deprecated. "
 *     + "This is scheduled to be removed in Gradle 8.0. "
 *     + "On method 'IncrementalTask.taskAction\$gradle_core' use 'org.gradle.work.InputChanges' instead. "
 *     + "Consult the upgrading guide for further information: "
 *     + "https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
 *   )
 * } else {
 *   error("AGP version changed, review deprecation warning suppression.")
 * }
 * ```
 * Notes:
 *  - Notice that there's no reference to AGP in the message, which means it'll suppress other usages of the same API!
 *  - The `gradleVersion` substitution is there so Gradle upgrades in 7.x don't need revising this warning.
 *  - The version check is optional, but encouraged, so hacks don't linger around longer than necessary.
 *  - The text is broken down into sentences, so it's more human-friendly in code.
 *
 * ### Example 2 - suppressing specific deprecation (Gradle 8+ only).
 * Realistic regex with stack trace example with Gradle 8.0 and Android Gradle Plugin 7.4:
 * ```kotlin
 * // Ignore warning for https://issuetracker.google.com/issues/264177800 since Gradle 8.0,
 * // it's going to be fixed in AGP 7.4.1 or AGP 8.0.
 * if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION < "7.4.1") {
 *   val gradleVersion: String = GradleVersion.current().version
 *   doNotNagAbout( // doNotNagAbout(Regex)
 *     Regex(
 *       Regex.escape(
 *         "The Report.destination property has been deprecated. "
 *         + "This is scheduled to be removed in Gradle 9.0. "
 *         + "Please use the outputLocation property instead. "
 *         + "See https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.reporting.Report.html#org.gradle.api.reporting.Report:destination for more details."
 *       ) + ".*${Regex.escape("at com.android.build.gradle.tasks.factory.AndroidUnitTest\$CreationAction.configure")}.*"
 *     )
 *   )
 * } else {
 *   error("AGP version changed, review deprecation warning suppression.")
 * }
 * ```
 * Notes (in addition to the notes from Example 1):
 *  - The stack trace is matched blanket as "contains" (`.*${...}.*`),
 *    and only one line is matched which identifies AGP as the source.
 *  - [Regex.escape] is used to prevent crazy-looking escape sequences.
 *
 * ### Example 3 - Partial match (discouraged)
 * For an example of partial match, sticking with Example 1, the regex could be:
 * ```kotlin
 * // doNotNagAbout(Regex)
 * doNotNagAbout(Regex("""^.*org\.gradle\.work\.InputChanges.*$"""))
 * ```
 * Note that this is possible, but discouraged,
 * because it may ignore too much and cause build breakages or delays in future upgrades of your builds.
 * That said, this is probably still better than using values other than `fail` for `org.gradle.warning.mode.
 *
 * @param message The regex provided will be used to match the entire message,
 *                including the stack trace in Gradle 8
 *                (depending on a flag, see `doNotNagAbout(String, String)` for more info).
 * If you want to do a partial match, add `.*` to fill in the dynamic parts.
 * The flag [RegexOption.DOT_MATCHES_ALL] is enforced so `.` will match newlines and the regex is easier to write.
 * This can be disabled with `(?-s)` inline if you know what you're doing.
 */
fun doNotNagAbout(message: Regex) {
	// In Gradle 4.7.0 (c633542) org.gradle.util.SingleMessageLogger#deprecatedFeatureHandler came to be in a refactor.
	// In Gradle 6.2.0 it was split (247fd32) to org.gradle.util.DeprecationLogger#deprecatedFeatureHandler
	// and then further split (308086a) to org.gradle.internal.deprecation.DeprecationLogger#deprecatedFeatureHandler
	// and then renamed (a75aedd) to #DEPRECATED_FEATURE_HANDLER.
	val loggerField =
		if (GradleVersion.version("6.2.0") <= GradleVersion.current().baseVersion) {
			Class.forName("org.gradle.internal.deprecation.DeprecationLogger")
				.getDeclaredField("DEPRECATED_FEATURE_HANDLER")
				.apply { isAccessible = true }
		} else if (GradleVersion.version("4.7.0") <= GradleVersion.current().baseVersion) {
			Class.forName("org.gradle.util.SingleMessageLogger")
				.getDeclaredField("deprecatedFeatureHandler")
				.apply { isAccessible = true }
		} else {
			error("Gradle ${GradleVersion.current()} too old, cannot ignore deprecation: $message")
		}
	val deprecationLogger: Any = loggerField.get(null)

	// LoggingDeprecatedFeatureHandler#messages was added in Gradle 1.8.
	val messagesField = LoggingDeprecatedFeatureHandler::class.java
		.getDeclaredField("messages")
		.apply { isAccessible = true }
	@Suppress("UNCHECKED_CAST")
	val messages: MutableSet<String> = messagesField.get(deprecationLogger) as MutableSet<String>

	val ignore = IgnoringSet.wrap(messages)
	messagesField.set(deprecationLogger, ignore)
	val regex = Regex(message.pattern, message.options + setOf(RegexOption.DOT_MATCHES_ALL))
	ignore.ignorePattern(regex)
}

/**
 * Surgically ignoring messages coming from Gradle deprecation system.
 *
 * This overload is a convenience for matching simple deprecations,
 * the whole string without interpreting it as a [Regex].
 *
 * @see doNotNagAbout for more details
 */
fun doNotNagAbout(message: String) {
	if (GradleVersion.version("8.0") <= GradleVersion.current().baseVersion) {
		// Ignoring with "startsWith" to disregard the stack trace. It's not ideal, but it's
		// the best we can do to counteract https://github.com/gradle/gradle/pull/22489 introduced in Gradle 8.0.
		doNotNagAbout(Regex("(?s)${Regex.escape(message)}.*"))
	} else {
		// In old versions, go for exact match.
		doNotNagAbout(Regex.fromLiteral(message))
	}
}

/**
 * Surgically ignoring messages coming from Gradle deprecation system.
 *
 * This overload is a convenience for matching simple deprecations coming from specific places.
 * For example rewriting Example 2 from [doNotNagAbout] to use this overload:
 * ```kotlin
 * doNotNagAbout( // doNotNagAbout(String, String)
 *   "The Report.destination property has been deprecated. "
 *     + "This is scheduled to be removed in Gradle 9.0. "
 *     + "Please use the outputLocation property instead. "
 *     + "See https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.reporting.Report.html#org.gradle.api.reporting.Report:destination for more details.",
 *   "at com.android.build.gradle.tasks.factory.AndroidUnitTest\$CreationAction.configure"
 * )
 * ```
 *
 * **Warning**: the stack trace matched against [stack] is not the full trace,
 * only the first 10 lines of it will be used.
 * See [LoggingDeprecatedFeatureHandler.displayDeprecationIfSameMessageNotDisplayedBefore].
 *
 * ## Enable stack traces
 *
 * **To use this method you need to enable stack traces for deprecations.**
 * For this we need to make sure [LoggingDeprecatedFeatureHandler.isTraceLoggingEnabled] is true.
 *
 * There are multiple ways to do this:
 *  * Add `-Dorg.gradle.deprecation.trace=true` to `org.gradle.jvmargs` in `gradle.properties`.
 *    Note: `org.gradle.deprecation.trace` is a system property for the Gradle daemon,
 *    not a Gradle property (-P) like others in the gradle.properties file.
 *  * Set `org.gradle.logging.stacktrace=all` in `gradle.properties` (equivalent command line `--stacktrace`/`-s`).
 *  * Set `org.gradle.logging.stacktrace=full` in `gradle.properties` (equivalent command line `--full-stacktrace`/`-S`).
 *
 * `org.gradle.deprecation.trace` is the least invasive options, because it only affects deprecations.
 *
 * `org.gradle.logging.stacktrace` options will affect how all build problems are reported.
 * A simple compile error will result in a big wall of stack trace. So this option is not recommended.
 *
 * @see doNotNagAbout for more details
 */
fun doNotNagAbout(message: String, stack: String) {
	if (GradleVersion.version("8.0") <= GradleVersion.current().baseVersion) {
		doNotNagAbout(Regex("(?s)${Regex.escape(message)}.*${Regex.escape(stack)}.*"))
	} else {
		error("Stack traces for deprecations are not available in ${GradleVersion.current()}.")
	}
}

/**
 * Surgically ignoring messages coming from Gradle deprecation system.
 *
 * This overload is a compatibility layer for potential Java / Groovy callers.
 *
 * @see doNotNagAbout for more details
 */
fun doNotNagAbout(message: Pattern) {
	doNotNagAbout(message.toRegex())
}

private class IgnoringSet(
	private val backingSet: MutableSet<String>
) : MutableSet<String> by backingSet {

	private val ignorePatterns: MutableSet<Regex> = mutableSetOf()

	fun ignorePattern(regex: Regex) {
		if (isDiagnosticsEnabled) {
			@Suppress("ForbiddenMethodCall") // This will be shown in the console, as the user explicitly asked for it.
			println("Ignoring pattern: ${regex}")
		}
		ignorePatterns.add(regex)
	}

	override fun add(element: String): Boolean {
		val isIgnored = ignorePatterns.any { it.matches(element) }
		val isNew = backingSet.add(element)
		if (isDiagnosticsEnabled) {
			val state = if (isNew) "first seen" else "already added"
			val ignores = ignorePatterns.joinToString(separator = "\n") { ignorePattern ->
				val matching = if (ignorePattern.matches(element)) "matching" else "not matching"
				"Deprecation is ${matching} ignore pattern:\n```regex\n${ignorePattern}\n```"
			}
			@Suppress("ForbiddenMethodCall") // This will be shown in the console, as the user explicitly asked for it.
			println("Nagging about ${state} deprecation:\n```\n${element}\n```\n${ignores}")
		}
		return !isIgnored && isNew
	}

	companion object {
		private val isDiagnosticsEnabled: Boolean
			get() = System.getProperty("net.twisterrob.gradle.nagging.diagnostics", "false").toBoolean()

		fun wrap(backingSet: MutableSet<String>): IgnoringSet =
			if (backingSet is IgnoringSet) backingSet else IgnoringSet(backingSet)
	}
}
