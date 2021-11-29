package net.twisterrob.gradle.android

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.kotlin.dsl.get

/**
 * New in AGP 4.2, stable in AGP 7.0.
 */
val Project.androidComponents: AndroidComponentsExtension<*, *, *>
	get() {
		if (!this.plugins.hasPlugin("com.android.base")) {
			throw PluginInstantiationException("Cannot use this before the Android plugins are applied.")
		}
		return this.extensions["androidComponents"] as AndroidComponentsExtension<*, *, *>
	}
