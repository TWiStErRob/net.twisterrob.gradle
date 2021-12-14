@file:JvmMultifileClass
@file:JvmName("AndroidHelpers")

package net.twisterrob.gradle.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestPlugin
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.scope.TaskContainer
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.addBuildConfigField40x
import net.twisterrob.gradle.internal.android.addBuildConfigField41x
import net.twisterrob.gradle.internal.android.addBuildConfigField42x
import net.twisterrob.gradle.internal.android.addBuildConfigField70x
import net.twisterrob.gradle.internal.android.manifestFile40x
import net.twisterrob.gradle.internal.android.manifestFile41x
import net.twisterrob.gradle.internal.android.taskContainerCompat40x
import net.twisterrob.gradle.internal.android.taskContainerCompat41x
import org.gradle.api.DomainObjectCollection
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.provider.Provider
import java.io.File
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

val BaseExtension.variants: DomainObjectSet<out @Suppress("DEPRECATION" /* AGP 7.0 */) com.android.build.gradle.api.BaseVariant>
	get() =
		when (this) {
			is AppExtension -> applicationVariants
			is LibraryExtension -> libraryVariants
			is TestExtension -> applicationVariants
			is TestedExtension -> testVariants
			else -> throw IllegalArgumentException("Unknown extension: $this")
		}

fun DomainObjectCollection<BuildType>.configure(name: String, block: (BuildType) -> Unit) {
	configureEach {
		if (it.name == name)
			block(it)
	}
}

fun Task.intermediateRegularFile(relativePath: String): RegularFileProperty =
	project.objects.fileProperty().apply {
		set(project.layout.buildDirectory
			.map { it.file("${AndroidProject.FD_INTERMEDIATES}/$relativePath") })
	}

val BaseVariantData.taskContainerCompat: TaskContainer
	get() =
		when {
			AGPVersions.CLASSPATH >= AGPVersions.v41x -> this.taskContainerCompat41x
			AGPVersions.CLASSPATH >= AGPVersions.v40x -> this.taskContainerCompat40x
			else -> AGPVersions.olderThan4NotSupported(AGPVersions.CLASSPATH)
		}

val ManifestProcessorTask.manifestFile: Provider<File>
	get() =
		when {
			AGPVersions.CLASSPATH >= AGPVersions.v41x -> this.manifestFile41x
			AGPVersions.CLASSPATH >= AGPVersions.v40x -> this.manifestFile40x
			else -> AGPVersions.olderThan4NotSupported(AGPVersions.CLASSPATH)
		}

/**
 * @see https://android-developers.googleblog.com/2020/12/announcing-android-gradle-plugin.html
 */
fun Project.addBuildConfigField(name: String, type: String, value: Provider<out Serializable>) {
	when {
		AGPVersions.CLASSPATH >= AGPVersions.v70x -> this.addBuildConfigField70x(name, type, value)
		AGPVersions.CLASSPATH compatible AGPVersions.v42x -> this.addBuildConfigField42x(name, type, value)
		AGPVersions.CLASSPATH compatible AGPVersions.v41x -> this.addBuildConfigField41x(name, type, value)
		AGPVersions.CLASSPATH compatible AGPVersions.v40x -> this.addBuildConfigField40x(name, type, value)
		else -> AGPVersions.otherThan4NotSupported(AGPVersions.CLASSPATH)
	}
}
