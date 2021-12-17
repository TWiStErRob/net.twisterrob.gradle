package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByName
import java.io.Serializable

/**
 * 7.0.x deprecated [com.android.build.api.extension.AndroidComponentsExtension].
 */
fun Project.addBuildConfigField70x(name: String, type: String, value: Provider<out Serializable>) {
	val androidComponents: AndroidComponentsExtension<*, *, *> =
		this.extensions.getByName<AndroidComponentsExtension<*, *, *>>("androidComponents")
	androidComponents.onVariants { variant ->
		variant.buildConfigFields.put(name, value.map { BuildConfigField(type = type, value = it, comment = null) })
	}
}
