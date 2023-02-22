package net.twisterrob.gradle.settings

import net.twisterrob.gradle.internal.deprecation.DeprecatedSettingsPlugin

internal class SettingsPluginDeprecated : DeprecatedSettingsPlugin(
	oldName = "net.twisterrob.settings",
	newName = "net.twisterrob.gradle.plugin.settings",
)
