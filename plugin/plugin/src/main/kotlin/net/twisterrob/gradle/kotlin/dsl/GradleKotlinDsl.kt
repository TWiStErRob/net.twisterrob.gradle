package net.twisterrob.gradle.kotlin.dsl

import net.twisterrob.gradle.Utils
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.kotlin.dsl.getPluginByName

/**
 * This should be `(this as dynamic).extensions as ExtensionContainer`, but `dynamic` is not allowed on JVM.
 * This version calls into Groovy so that Gradle's custom handlers (Decorated?) can respond correctly.
 */
internal val Any.extensions get() = Utils.getExtensions(this)

internal val Project.base: BasePluginConvention get() = convention.getPluginByName("base")
