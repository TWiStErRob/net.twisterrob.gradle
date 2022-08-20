package net.twisterrob.gradle.plugins.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply

class SettingsPlugin : Plugin<Settings> {
	override fun apply(settings: Settings) {
		settings.apply<AcceptEnterpriseTOSPlugin>()
	}
}
