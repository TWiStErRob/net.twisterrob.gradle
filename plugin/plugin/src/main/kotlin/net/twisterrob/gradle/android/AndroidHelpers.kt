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
import net.twisterrob.gradle.common.ANDROID_GRADLE_PLUGIN_VERSION
import org.gradle.api.DomainObjectCollection
import org.gradle.api.DomainObjectSet
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.provider.Provider
import java.io.File

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
fun BaseVariant.toDebugString() = (
		"${this::class}"
				+ ", name=$name, desc=$description, base=$baseName, dir=$dirName, pkg=$applicationId, flav=$flavorName"
				+ (if (this is VersionedVariant) ", ver=$versionName, code=$versionCode" else "")
		)

fun DomainObjectCollection<BuildType>.configure(name: String, block: (BuildType) -> Unit) =
	configureEach {
		if (it.name == name)
			block(it)
	}

fun Task.intermediateRegularFile(relativePath: String): RegularFileProperty =
	project.objects.fileProperty().apply {
		set(project.layout.buildDirectory
			.map { it.file("${AndroidProject.FD_INTERMEDIATES}/$relativePath") })
	}

val BaseVariantData.taskContainerCompat: TaskContainer
	get() =
		when {
			ANDROID_GRADLE_PLUGIN_VERSION >= "4.1.0" ->
				taskContainer
			ANDROID_GRADLE_PLUGIN_VERSION >= "4.0.0" ->
				// Call reflectively, because return type changed
				// from TaskContainer interface to MutableTaskContainer class.
				BaseVariantData::class.java
					.getDeclaredMethod("getTaskContainer")
					.invoke(this) as TaskContainer
			else ->
				TODO("3.x not supported")
		}

val ManifestProcessorTask.manifestFile: Provider<File>
	get() =
		when {
			ANDROID_GRADLE_PLUGIN_VERSION >= "4.1.0" ->
				when (this) {
					is ProcessApplicationManifest -> mergedManifest.asFile
					is ProcessMultiApkApplicationManifest -> mainMergedManifest.asFile
					else -> error("$this is an unsupported ${ManifestProcessorTask::class}")
				}
			ANDROID_GRADLE_PLUGIN_VERSION >= "4.0.0" -> {
				val manifestOutputDirectory =
					Class.forName("com.android.build.gradle.tasks.ManifestProcessorTask")
						.getDeclaredMethod("getManifestOutputDirectory")
						.invoke(this) as DirectoryProperty
				manifestOutputDirectory
					.file("AndroidManifest.xml")
					.map { it.asFile }
			}
			else ->
				error("AGP 3.x is not supported")
		}
