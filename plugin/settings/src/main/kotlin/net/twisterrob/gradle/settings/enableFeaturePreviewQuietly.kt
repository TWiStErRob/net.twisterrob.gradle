@file:JvmMultifileClass
@file:JvmName("SettingsUtils")

package net.twisterrob.gradle.settings

import org.gradle.api.initialization.Settings
import org.gradle.util.GradleVersion

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
	val incubationLogger = when {
		GradleVersion.version("7.1.0") <= GradleVersion.current().baseVersion ->
			// Moved to internal package in 7.1.0
			// https://github.com/gradle/gradle/commit/d70f06dc97eb5c840da85db6e8f112e58773f98d#diff-bf83d0d5d00874535bbc06720f025e565fc932282c5eabb0530ad27e93c57739R17
			org.gradle.util.internal.IncubationLogger::class.java
		GradleVersion.version("6.2.0") <= GradleVersion.current().baseVersion ->
			// Renamed from SingleMessageLogger in
			// https://github.com/gradle/gradle/commit/247fd3238c434ddcf8e40d4cbf8342eb190c22e6#diff-3dcbf4f85d08fb90352574bd5f51196d85e592c131cebdab4efbfe857129997f
			// Renamed from incubatingFeatureHandler to INCUBATING_FEATURE_HANDLER in
			// https://github.com/gradle/gradle/commit/a75aeddff32c6ac9cb3d889d8ea551ea9babe04e#diff-3dcbf4f85d08fb90352574bd5f51196d85e592c131cebdab4efbfe857129997fR28
			// Using Class.forName, because compiling against latest Gradle which doesn't have this class.
			Class.forName("org.gradle.util.IncubationLogger")
		else ->
			error(
				"Cannot find incubating feature handler. " +
						"It was SingleMessageLogger before 6.2.0, " +
						"but that's not supported yet, please raise an issue."
			)
	}

	val logger: Any = incubationLogger
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
