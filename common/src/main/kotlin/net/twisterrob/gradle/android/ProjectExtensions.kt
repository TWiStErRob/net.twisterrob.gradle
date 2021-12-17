package net.twisterrob.gradle.android

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.kotlin.dsl.get

/**
 * New in AGP 4.2, stable in AGP 7.0.
 */
val Project.androidComponents: AndroidComponentsExtension<*, *, *>
	get() {
		// REPORT hasPlugin("com.android.base") should be equivalent, but returns false during plugins.withType<ABP> { }
		// because com.android.build.gradle.internal.plugins.BasePlugin applies ABP class not ID?
		if (!this.plugins.hasPlugin(AndroidBasePlugin::class.java)) {
			throw PluginInstantiationException("Cannot use this before the Android plugins are applied.")
		}
		return this.extensions["androidComponents"] as AndroidComponentsExtension<*, *, *>
	}
