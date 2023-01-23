@file:JvmMultifileClass
@file:JvmName("GradleUtils")

package net.twisterrob.gradle

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
 *    at that point the messages may contain the stack trace of the occurrence (depending on --stacktrace flag).
 *    This gives us the ability to specifically ignore an instance of a type of message.
 *    This is useful if multiple plugins are behind on fixing the deprecations.
 *  - `fail` was not a valid option for `--warning-mode` before Gradle 5.6.0.
 *
 * Realistic usage example with Gradle 7.5.1 and Android Gradle Plugin 7.2.2:
 * ```kotlin
 * // Ignore warning for https://issuetracker.google.com/issues/218478028 since Gradle 7.5,
 * // it's going to be fixed in AGP 7.3.
 * if (com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION < "7.3") {
 *   val gradleVersion: String = GradleVersion.current().version
 *   doNotNagAbout(
 *     "IncrementalTaskInputs has been deprecated. "
 *     + "This is scheduled to be removed in Gradle 8.0. "
 *     + "On method 'IncrementalTask.taskAction\$gradle_core' use 'org.gradle.work.InputChanges' instead. "
 *     + "Consult the upgrading guide for further information: "
 *     + "https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
 *   )
 * } else {
 * 	 error("AGP version changed, review deprecation warning suppression.")
 * }
 * ```
 * Notes:
 *  - The `gradleVersion` substitution is there so Gradle upgrades in 7.x don't need revising this warning.
 *  - The version check is optional, but encouraged, so hacks don't linger around longer than necessary.
 *
 * See `doNotNagAbout(Regex)` overload for more dynamic message matching.
 *
 * @param message The regex provided will be used to match the entire message, including the stack trace in Gradle 8.
 * If you want to do a partial match, use `^` and `$` anchors and add `.*` to fill in the dynamic parts.
 *
 * For example sticking with the example, the regex could be:
 * ```kotlin
 * doNotNagAbout(Regex("""^.*org\.gradle\.work\.InputChanges.*$"""))
 * ```
 *
 * Note that this is possible, but discouraged,
 * because it may ignore too much and cause build breakages or delays in future upgrades of your builds.
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
	val messagesField = org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler::class.java
		.getDeclaredField("messages")
		.apply { isAccessible = true }
	@Suppress("UNCHECKED_CAST")
	val messages: MutableSet<String> = messagesField.get(deprecationLogger) as MutableSet<String>

	val ignore = IgnoringSet.wrap(messages)
	messagesField.set(deprecationLogger, ignore)
	ignore.ignorePattern(message)
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

	private val ignores: MutableSet<Regex> = mutableSetOf()

	fun ignorePattern(regex: Regex) {
		ignores.add(regex)
	}

	override fun add(element: String): Boolean {
		val isIgnored = ignores.any { it.matches(element) }
		val isNew = backingSet.add(element)
		return !isIgnored && isNew
	}

	companion object {
		fun wrap(backingSet: MutableSet<String>): IgnoringSet =
			if (backingSet is IgnoringSet) backingSet else IgnoringSet(backingSet)
	}
}
