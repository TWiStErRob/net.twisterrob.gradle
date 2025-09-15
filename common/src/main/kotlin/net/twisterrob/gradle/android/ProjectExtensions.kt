package net.twisterrob.gradle.android

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.kotlin.dsl.getByName

/**
 * No API constant in AGP.
 * Hardcoded in
 *  * [com.android.build.gradle.internal.plugins.AppPlugin.createComponentExtension]
 *  * [com.android.build.gradle.internal.plugins.LibraryPlugin.createComponentExtension]
 *  * [com.android.build.gradle.internal.plugins.TestPlugin.createComponentExtension]
 *  * [com.android.build.gradle.internal.plugins.DynamicFeaturePlugin.createComponentExtension]
 */
private const val ANDROID_COMPONENTS = "androidComponents"

fun Project.androidComponents(block: Action<AndroidComponentsExtension<Any, *, *>>) {
	block.execute(this.androidComponents)
}

val Project.androidComponents: AndroidComponentsExtension<Any, *, *>
	get() {
		// REPORT hasPlugin("com.android.base") should be equivalent, but returns false during plugins.withType<ABP> { }
		// because com.android.build.gradle.internal.plugins.BasePlugin applies ABP class not ID?
		if (!this.plugins.hasPlugin(AndroidBasePlugin::class.java)) {
			throw PluginInstantiationException("Cannot use this before the Android plugins are applied.")
		}
		return this.extensions.getByName<AndroidComponentsExtension<Any, *, *>>(ANDROID_COMPONENTS)
	}

fun Project.androidComponentsApplication(block: Action<ApplicationAndroidComponentsExtension>) {
	block.execute(this.androidComponentsApplication)
}

val Project.androidComponentsApplication: ApplicationAndroidComponentsExtension
	get() {
		if (!this.plugins.hasPlugin(AppPlugin::class.java)) {
			throw PluginInstantiationException("Cannot use this without the Android application plugin being applied.")
		}
		return this.extensions.getByName<ApplicationAndroidComponentsExtension>(ANDROID_COMPONENTS)
	}

fun Project.androidComponentsLibrary(block: Action<LibraryAndroidComponentsExtension>) {
	block.execute(this.androidComponentsLibrary)
}

val Project.androidComponentsLibrary: LibraryAndroidComponentsExtension
	get() {
		if (!this.plugins.hasPlugin(LibraryPlugin::class.java)) {
			throw PluginInstantiationException("Cannot use this without the Android library plugin being applied.")
		}
		return this.extensions.getByName<LibraryAndroidComponentsExtension>(ANDROID_COMPONENTS)
	}
