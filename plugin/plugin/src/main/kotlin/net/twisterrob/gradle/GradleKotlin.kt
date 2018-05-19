package net.twisterrob.gradle

import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer

@Suppress("UNCHECKED_CAST")
operator fun <T> ExtensionContainer.get(name: String): T = getByName(name) as T

operator fun ConfigurationContainer.get(name: String): Configuration = getAt(name)

inline fun <reified T : Plugin<*>> PluginContainer.apply(): T = apply<T>(T::class.java)
