@file:JvmMultifileClass
@file:JvmName("GradleUtils")

package net.twisterrob.gradle

import org.gradle.util.GradleVersion

/**
 * Surgically ignoring messages like this will prevent actual executions from triggering
 * stack traces and warnings, which means that even with some warnings,
 * it's possible to use `org.gradle.warning.mode=fail`.
 *
 * Example usage with Gradle 7.5.1 and Android Gradle Plugin 7.2.2:
 * ```
 * val gradleVersion: String = GradleVersion.current().version
 *
 * // Ignore warning for https://issuetracker.google.com/issues/218478028 since Gradle 7.5,
 * // it's going to be fixed in AGP 7.3.
 * doNotNagAbout(
 *   "IncrementalTaskInputs has been deprecated. "
 *   + "This is scheduled to be removed in Gradle 8.0. "
 *   + "On method 'IncrementalTask.taskAction\$gradle_core' use 'org.gradle.work.InputChanges' instead. "
 *   + "Consult the upgrading guide for further information: "
 *   + "https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
 * )
 * ```
 * Note: the gradleVersion substitution is there so Gradle upgrades in 7.x don't need revising this warning.
 */
fun doNotNagAbout(message: String) {
	// "fail" was not a valid option for --warning-mode before Gradle 5.6.0.
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

	val ignore = if (messages is IgnoringSet) messages else IgnoringSet(messages)
	messagesField.set(deprecationLogger, ignore)
	// Ignoring with "startsWith" to disregard the stack trace. It's not ideal,
	// but it's the best we can do to counteract https://github.com/gradle/gradle/pull/22489 introduced in Gradle 8.0.
	ignore.ignorePattern(Regex("(?s)${Regex.escape(message)}.*"))
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
}
