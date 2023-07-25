package net.twisterrob.gradle.nagging

import net.twisterrob.gradle.nagging.internal.reviewIfNaggingCausesFailure
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * Applying the plugin will put the classes and functions in this jar to the classpath of Settings.
 * ```
 * plugins {
 *    id("net.twisterrob.gradle.plugin.nagging")
 * }
 * ```
 */
class NaggingPlugin : Plugin<Settings> {
	override fun apply(settings: Settings) {
		settings.gradle.reviewIfNaggingCausesFailure()
	}
}
