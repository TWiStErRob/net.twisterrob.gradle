/**
 * Surgically ignoring messages like this will prevent actual executions from triggering
 * stack traces and warnings, which means that even with some warnings,
 * it's possible to use org.gradle.warning.mode=fail.
 */
public fun doNotNagAbout(message: String) {
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
