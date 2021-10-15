@file:JvmMultifileClass
@file:JvmName("AndroidHelpers")

package net.twisterrob.gradle.android

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.extension.AndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.common.ANDROID_GRADLE_PLUGIN_VERSION
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByName
import java.io.Serializable

/**
 * @see https://android-developers.googleblog.com/2020/12/announcing-android-gradle-plugin.html
 */
fun Project.addBuildConfigField(name: String, type: String, value: Provider<out Serializable>) {
	when {
		ANDROID_GRADLE_PLUGIN_VERSION >= "4.2.0" -> addBuildConfigField420(name, type, value)
		ANDROID_GRADLE_PLUGIN_VERSION >= "4.1.0" -> addBuildConfigField410(name, type, value)
		ANDROID_GRADLE_PLUGIN_VERSION >= "4.0.0" -> addBuildConfigField400(name, type, value)
		else -> error("AGP 3.x not supported")
	}
}

@Suppress("UnstableApiUsage")
private fun Project.addBuildConfigField400(name: String, type: String, value: Provider<out Any?>) {
	val android: BaseExtension = this.extensions.getByName<BaseExtension>("android")
	android.defaultConfig.buildConfigField(name = name, type = type, value = value.map { it.toString() }.get())
}

@Suppress("UnstableApiUsage")
private fun Project.addBuildConfigField410(name: String, type: String, value: Provider<out Serializable>) {
	val android: BaseExtension = this.extensions.getByName<BaseExtension>("android")
	// Reflective hack for:
	// (android as CommonExtension<*, *, *, *, *, *, *, *>).onVariantProperties { this.buildConfigFields.put(...) }
	// VariantProperties class was removed in 4.2.0, so need to reflectively do everything
	// because it's on the Action generic arguments and need to call a method on it.
	val onVariantProperties = CommonExtension::class.java
		.getDeclaredMethod("onVariantProperties", Action::class.java)
	onVariantProperties.invoke(android, Action<Any> { variant ->
		@Suppress("LocalVariableName")
		val VariantProperties = Class.forName("com.android.build.api.variant.VariantProperties")
		val getBuildConfigFields = VariantProperties.getDeclaredMethod("getBuildConfigFields")
		@Suppress("UNCHECKED_CAST")
		val buildConfigFields = getBuildConfigFields
			.invoke(variant) as MapProperty<String, BuildConfigField<out Serializable>>
		buildConfigFields.put(name, value.map { BuildConfigField(type = type, value = it, comment = null) })
	})
}

@Suppress("UnstableApiUsage")
private fun Project.addBuildConfigField420(name: String, type: String, value: Provider<out Serializable>) {
	val androidComponents: AndroidComponentsExtension<*, *> =
		this.extensions.getByName<AndroidComponentsExtension<*, *>>("androidComponents")
	androidComponents.onVariants { variant ->
		variant.buildConfigFields.put(name, value.map { BuildConfigField(type = type, value = it, comment = null) })
	}
}
