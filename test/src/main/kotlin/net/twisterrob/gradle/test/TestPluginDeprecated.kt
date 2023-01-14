package net.twisterrob.gradle.test

import net.twisterrob.gradle.common.DeprecatedPlugin

internal class TestPluginDeprecated : DeprecatedPlugin(
	originalName = "net.twisterrob.gradle.test",
	newName = "net.twisterrob.gradle.plugin.test",
)
