package net.twisterrob.gradle.kotlin.dsl

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer
import org.gradle.kotlin.dsl.getPluginByName

// TODEL if https://github.com/gradle/kotlin-dsl/issues/1154 goes through
internal val Any.extensions: ExtensionContainer get() = (this as ExtensionAware).extensions

internal val Project.base: BasePluginConvention get() = convention.getPluginByName("base")

/**
 * Configures the [Plugin] with the given [pluginId] using [action], similar to [PluginContainer.withId], but type safe.
 *
 * @param P The assumed type of [Plugin].
 * @param pluginId The ID of the [Plugin] to find.
 * @param action The action to execute for the plugin (restrictions apply).
 * @see [org.gradle.kotlin.dsl.withType] for a similar implementation
 * @see [PluginContainer.withId] for the wrapped method
 */
inline fun <reified P : Plugin<*>> PluginContainer.withId(pluginId: String, crossinline action: P.() -> Unit) =
	withId(pluginId) {
		(it as P).action()
	}
