@file:JvmMultifileClass
@file:JvmName("SettingsUtils")

package net.twisterrob.gradle.settings

import org.gradle.api.initialization.Settings

/**
 * Enables a feature flag in Gradle via [Settings.enableFeaturePreview],
 * but also avoids printing a warning every time the feature is used.
 *
 * See [Feature request](https://github.com/gradle/gradle/issues/19069) that would make this unnecessary.
 *
 * @param name Name of the feature to enable, same as [Settings.enableFeaturePreview].
 * @param summary Summary of the feature that gets logged to the console every time the feature is used.
 * The summary can be found in Gradle output,
 * or as usages of [org.gradle.util.internal.IncubationLogger.incubatingFeatureUsed].
 * See [GitHub](https://github.com/gradle/gradle/search?q=incubatingFeatureUsed) for examples in most recent Gradle.
 */
fun Settings.enableFeaturePreviewQuietly(name: String, summary: String) {
	enableFeaturePreview(name)
	val logger: Any = org.gradle.util.internal.IncubationLogger::class.java
		.getDeclaredField("INCUBATING_FEATURE_HANDLER")
		.apply { isAccessible = true }
		.get(null)

	@Suppress("UNCHECKED_CAST")
	val features: MutableSet<String> = org.gradle.internal.featurelifecycle.LoggingIncubatingFeatureHandler::class.java
		.getDeclaredField("features")
		.apply { isAccessible = true }
		.get(logger) as MutableSet<String>

	features.add(summary)
}
