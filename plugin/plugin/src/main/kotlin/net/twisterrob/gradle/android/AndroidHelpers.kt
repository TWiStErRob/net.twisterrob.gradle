package net.twisterrob.gradle.android

import com.android.build.api.extension.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.PluginContainer
import org.gradle.kotlin.dsl.getByName
import java.io.Serializable

fun PluginContainer.hasAndroid(): Boolean =
	hasPlugin(AppPlugin::class.java) ||
			hasPlugin(LibraryPlugin::class.java) ||
			hasAndroidTest()

fun PluginContainer.hasAndroidTest() = hasPlugin(TestPlugin::class.java)

/**
 * @see https://android-developers.googleblog.com/2020/12/announcing-android-gradle-plugin.html
 */
@Suppress("UnstableApiUsage") // added in 4.2
fun Project.onVariantProperties(action: ApplicationVariant.() -> Unit) {
	//if (< 4.2.0) {
	//	val android: BaseExtension =
	//		this.extensions.getByName<BaseExtension>("android")
	//	fun BaseExtension.onVariantProperties(action: VariantProperties.() -> Unit) {
	//		(this as CommonExtension<*, *, *, *, *, *, *, *>).onVariantProperties(action)
	//	}
	//}
	val androidComponents: ApplicationAndroidComponentsExtension =
		this.extensions.getByName<ApplicationAndroidComponentsExtension>("androidComponents")
	androidComponents.onVariants { action(it) }
}

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
