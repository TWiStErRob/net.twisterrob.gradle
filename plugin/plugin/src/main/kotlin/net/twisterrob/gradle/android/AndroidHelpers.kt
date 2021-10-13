@file:JvmMultifileClass
@file:JvmName("AndroidHelpers")

package net.twisterrob.gradle.android

import com.android.build.api.variant.BuildConfigField
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
import com.android.builder.model.AndroidProject
import org.gradle.api.DomainObjectCollection
import org.gradle.api.DomainObjectSet
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.PluginContainer
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

fun <T : Serializable> RegularFile.asBuildConfigField(
	type: String,
	valueMapper: (String) -> T
): BuildConfigField<T> =
	BuildConfigField(
		type = type,
		value = valueMapper(this.asFile.readText()),
		comment = null
	)

fun Task.intermediateRegularFile(relativePath: String): RegularFileProperty =
	project.objects.fileProperty().apply {
		set(project.layout.buildDirectory
			.map { it.file("${AndroidProject.FD_INTERMEDIATES}/$relativePath") })
	}
