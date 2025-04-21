package net.twisterrob.gradle.settings

import net.twisterrob.gradle.internal.deprecation.DeprecatedSettingsPlugin

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
internal abstract class SettingsPluginDeprecated : DeprecatedSettingsPlugin(
	oldName = "net.twisterrob.settings",
	newName = "net.twisterrob.gradle.plugin.settings",
)
