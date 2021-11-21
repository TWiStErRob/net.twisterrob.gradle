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
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.VersionedVariant
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.scope.TaskContainer
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.build.gradle.tasks.ProcessApplicationManifest
import com.android.build.gradle.tasks.ProcessMultiApkApplicationManifest
import com.android.builder.model.AndroidProject
import net.twisterrob.gradle.android.internal.addBuildConfigField40x
import net.twisterrob.gradle.android.internal.addBuildConfigField41x
import net.twisterrob.gradle.android.internal.addBuildConfigField42x
import net.twisterrob.gradle.common.AGPVersions
import org.gradle.api.DomainObjectCollection
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
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

val BaseExtension.variants: DomainObjectSet<out BaseVariant>
	get() =
		when (this) {
			is AppExtension -> applicationVariants
			is LibraryExtension -> libraryVariants
			is TestExtension -> applicationVariants
			is TestedExtension -> testVariants
			else -> throw IllegalArgumentException("Unknown extension: $this")
		}

@Suppress("unused")
fun BaseVariant.toDebugString(): String =
	buildString {
		append(this@toDebugString::class)
		append(", name=$name, desc=$description, base=$baseName, dir=$dirName, pkg=$applicationId, flav=$flavorName")
		append(if (this@toDebugString is VersionedVariant) ", ver=$versionName, code=$versionCode" else "")
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
			AGPVersions.CLASSPATH >= AGPVersions.v41x ->
				taskContainer
			AGPVersions.CLASSPATH >= AGPVersions.v40x ->
				// Call reflectively, because return type changed
				// from TaskContainer interface to MutableTaskContainer class.
				BaseVariantData::class.java
					.getDeclaredMethod("getTaskContainer")
					.invoke(this) as TaskContainer
			else -> AGPVersions.olderThan4NotSupported(AGPVersions.CLASSPATH)
		}

val ManifestProcessorTask.manifestFile: Provider<File>
	get() =
		when {
			AGPVersions.CLASSPATH >= AGPVersions.v41x ->
				when (this) {
					is ProcessApplicationManifest -> mergedManifest.asFile
					is ProcessMultiApkApplicationManifest -> mainMergedManifest.asFile
					else -> error("$this is an unsupported ${ManifestProcessorTask::class}")
				}
			AGPVersions.CLASSPATH >= AGPVersions.v40x -> {
				val manifestOutputDirectory =
					Class.forName("com.android.build.gradle.tasks.ManifestProcessorTask")
						.getDeclaredMethod("getManifestOutputDirectory")
						.invoke(this) as DirectoryProperty
				manifestOutputDirectory
					.file("AndroidManifest.xml")
					.map { it.asFile }
			}
			else -> AGPVersions.olderThan4NotSupported(AGPVersions.CLASSPATH)
		}

/**
 * @see https://android-developers.googleblog.com/2020/12/announcing-android-gradle-plugin.html
 */
fun Project.addBuildConfigField(name: String, type: String, value: Provider<out Serializable>) {
	when {
		AGPVersions.CLASSPATH compatible AGPVersions.v42x -> addBuildConfigField42x(name, type, value)
		AGPVersions.CLASSPATH compatible AGPVersions.v41x -> addBuildConfigField41x(name, type, value)
		AGPVersions.CLASSPATH compatible AGPVersions.v40x -> addBuildConfigField40x(name, type, value)
		else -> AGPVersions.otherThan4NotSupported(AGPVersions.CLASSPATH)
	}
}
