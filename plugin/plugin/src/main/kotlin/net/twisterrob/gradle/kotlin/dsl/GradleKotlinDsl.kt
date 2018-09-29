package net.twisterrob.gradle.kotlin.dsl

import net.twisterrob.gradle.Utils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer
import org.gradle.kotlin.dsl.getPluginByName

/**
 * This should be `(this as dynamic).extensions as ExtensionContainer`, but `dynamic` is not allowed on JVM.
 * This version calls into Groovy so that Gradle's custom handlers (Decorated?) can respond correctly.
 */
internal val Any.extensions: ExtensionContainer get() = Utils.getExtensions(this)

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
