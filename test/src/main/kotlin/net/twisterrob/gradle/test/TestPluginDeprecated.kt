package net.twisterrob.gradle.test

import net.twisterrob.gradle.common.DeprecatedProjectPlugin

internal class TestPluginDeprecated : DeprecatedProjectPlugin(
	oldName = "net.twisterrob.gradle.test",
	newName = "net.twisterrob.gradle.plugin.test",
)
