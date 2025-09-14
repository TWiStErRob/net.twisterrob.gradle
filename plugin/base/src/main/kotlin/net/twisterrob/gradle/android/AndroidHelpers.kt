@file:JvmMultifileClass
@file:JvmName("AndroidHelpers")

package net.twisterrob.gradle.android

import com.android.SdkConstants
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByName
import java.io.Serializable

/**
 * Checks if there are Android plugins applied.
 *
 * Note using the `META-INF/gradle-plugins/\*.properties` Plugin ID representations
 * to not require Android to be on classpath just to see if it's applied.
 * @see AppPlugin
 * @see LibraryPlugin
 * @see TestPlugin
 */
fun PluginContainer.hasAndroid(): Boolean =
	hasPlugin("com.android.application")
			|| hasPlugin("com.android.library")
			|| hasAndroidTest()

/**
 * Checks if there is an Android test plugin applied.
 *
 * Note using the `META-INF/gradle-plugins/\*.properties` Plugin ID representations
 * to not require Android to be on classpath just to see if it's applied.
 * @see TestPlugin
 */
fun PluginContainer.hasAndroidTest(): Boolean =
	hasPlugin("com.android.test")

fun DomainObjectCollection<BuildType>.configure(name: String, block: (BuildType) -> Unit) {
	configureEach { buildType ->
		if (buildType.name == name) {
			block(buildType)
		}
	}
}

fun Project.intermediateRegularFile(relativePath: String): Provider<RegularFile> =
	this.layout.buildDirectory.file("${SdkConstants.FD_INTERMEDIATES}/$relativePath")

/**
 * @see https://android-developers.googleblog.com/2020/12/announcing-android-gradle-plugin.html
 */
fun Project.addBuildConfigField(name: String, type: String, value: Provider<out Serializable>) {
	val androidComponents: AndroidComponentsExtension<*, *, *> =
		this.extensions.getByName<AndroidComponentsExtension<*, *, *>>("androidComponents")
	androidComponents.onVariants { variant ->
		variant
			.buildConfigFields
			?.put(name, value.map { BuildConfigField(type = type, value = it, comment = null) })
	}
}
