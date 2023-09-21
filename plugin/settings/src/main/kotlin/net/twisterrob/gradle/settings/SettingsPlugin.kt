package net.twisterrob.gradle.settings

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
@Suppress("UnnecessaryAbstractClass") // Gradle convention.
abstract class SettingsPlugin : Plugin<Settings> {
	override fun apply(settings: Settings) {
		// Nothing to do, yet.
	}
}
