package net.twisterrob.gradle.settings

import net.twisterrob.gradle.internal.nagging.allowUnlimitedStacksForNagging
import net.twisterrob.gradle.internal.nagging.reviewIfNaggingCausesFailure
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * Applying the plugin will put the classes and functions in this jar to the classpath of Settings.
 * ```
 * plugins {
 *    id("net.twisterrob.settings")
 * }
 * ```
 */
class SettingsPlugin : Plugin<Settings> {
	override fun apply(settings: Settings) {
		allowUnlimitedStacksForNagging()
		reviewIfNaggingCausesFailure(settings.gradle)
	}
}
