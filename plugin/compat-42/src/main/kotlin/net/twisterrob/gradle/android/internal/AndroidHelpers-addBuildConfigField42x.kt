package net.twisterrob.gradle.android.internal

import com.android.build.api.extension.AndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByName
import java.io.Serializable

@Suppress("UnstableApiUsage")
fun Project.addBuildConfigField42x(name: String, type: String, value: Provider<out Serializable>) {
	val androidComponents: AndroidComponentsExtension<*, *> =
		this.extensions.getByName<AndroidComponentsExtension<*, *>>("androidComponents")
	androidComponents.onVariants { variant ->
		variant.buildConfigFields.put(name, value.map { BuildConfigField(type = type, value = it, comment = null) })
	}
}
