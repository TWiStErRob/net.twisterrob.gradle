@file:JvmMultifileClass
@file:JvmName("AndroidHelpers")

package net.twisterrob.gradle.android

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.extension.AndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import net.twisterrob.gradle.common.ANDROID_GRADLE_PLUGIN_VERSION
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.kotlin.dsl.getByName
import java.io.Serializable

@Suppress("UnstableApiUsage")
interface CompatibleVariantProperties {
	val buildConfigFields: MapProperty<String, BuildConfigField<out Serializable>>
}

/**
 * @see https://android-developers.googleblog.com/2020/12/announcing-android-gradle-plugin.html
 */
fun Project.onVariantProperties(action: CompatibleVariantProperties.() -> Unit) {
	when {
		ANDROID_GRADLE_PLUGIN_VERSION >= "4.2.0" -> onVariantProperties420(action)
		ANDROID_GRADLE_PLUGIN_VERSION >= "4.0.0" -> onVariantProperties400(action)
		else -> TODO("AGP 3.x not supported")
	}
}

@Suppress("UnstableApiUsage")
private fun Project.onVariantProperties400(action: CompatibleVariantProperties.() -> Unit) {
	// Reflective hack for:
	// (android as CommonExtension<*, *, *, *, *, *, *, *>).onVariantProperties { action(delegate(it)) }
	// VariantProperties class was removed in 4.2.0, so need to reflectively do everything
	// because it's on the Action generic arguments and need to call a method on it.
	val android: BaseExtension = this.extensions.getByName<BaseExtension>("android")
	val onVariantProperties = CommonExtension::class.java
		.getDeclaredMethod("onVariantProperties", Action::class.java)
	onVariantProperties.invoke(android, Action<Any> {
		action(createVariantPropertiesDelegate400(it))
	})
}

@Suppress("UnstableApiUsage")
private fun createVariantPropertiesDelegate400(variant: Any): CompatibleVariantProperties {
	@Suppress("LocalVariableName")
	val VariantProperties = Class.forName("com.android.build.api.variant.VariantProperties")
	val buildConfigFields = VariantProperties.getDeclaredMethod("getBuildConfigFields")
	return object : CompatibleVariantProperties {
		override val buildConfigFields: MapProperty<String, BuildConfigField<out Serializable>>
			@Suppress("UNCHECKED_CAST")
			get() = buildConfigFields.invoke(variant) as MapProperty<String, BuildConfigField<out Serializable>>
	}
}

@Suppress("UnstableApiUsage")
private fun Project.onVariantProperties420(action: CompatibleVariantProperties.() -> Unit) {
	val androidComponents: AndroidComponentsExtension<*, *> =
		this.extensions.getByName<AndroidComponentsExtension<*, *>>("androidComponents")
	androidComponents.onVariants {
		action(createVariantPropertiesDelegate420(it))
	}
}

@Suppress("UnstableApiUsage")
private fun createVariantPropertiesDelegate420(variant: Variant): CompatibleVariantProperties =
	object : CompatibleVariantProperties {
		override val buildConfigFields: MapProperty<String, BuildConfigField<out Serializable>>
			get() = variant.buildConfigFields
	}
