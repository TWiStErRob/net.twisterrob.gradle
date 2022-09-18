@file:JvmMultifileClass
@file:JvmName("GradleUtils")

package net.twisterrob.gradle

/**
 * Surgically ignoring messages like this will prevent actual executions from triggering
 * stack traces and warnings, which means that even with some warnings,
 * it's possible to use `org.gradle.warning.mode=fail`.
 *
 * Example usage with Gradle 7.5.1 and Android Gradle Plugin 7.2.2:
 * ```
 * val gradleVersion: String = GradleVersion.current().baseVersion.version
 *
 * // Ignore warning for https://issuetracker.google.com/issues/218478028 since Gradle 7.5,
 * // it's going to be fixed in AGP 7.3.
 * doNotNagAbout(
 *     "IncrementalTaskInputs has been deprecated. "
 *     + "This is scheduled to be removed in Gradle 8.0. "
 *     + "On method 'IncrementalTask.taskAction\$gradle_core' use 'org.gradle.work.InputChanges' instead. "
 *     + "Consult the upgrading guide for further information: "
 *     + "https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
 * )
 * ```
 * Note: the gradleVersion substitution is there so Gradle upgrades in 7.x don't need revising this warning.
 */
fun doNotNagAbout(message: String) {
	val logger: Any = org.gradle.internal.deprecation.DeprecationLogger::class.java
		.getDeclaredField("DEPRECATED_FEATURE_HANDLER")
		.apply { isAccessible = true }
		.get(null)

	@Suppress("UNCHECKED_CAST")
	val messages: MutableSet<String> =
		org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler::class.java
			.getDeclaredField("messages")
			.apply { isAccessible = true }
			.get(logger) as MutableSet<String>

	messages.add(message)
}
