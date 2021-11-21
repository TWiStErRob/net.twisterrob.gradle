package net.twisterrob.gradle.android.internal

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.BuildConfigField
import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByName
import java.io.Serializable

@Suppress("UnstableApiUsage")
fun Project.addBuildConfigField41x(name: String, type: String, value: Provider<out Serializable>) {
	val android: BaseExtension = this.extensions.getByName<BaseExtension>("android")
	(android as CommonExtension<*, *, *, *, *, *, *, *>).onVariantProperties {
		this.buildConfigFields.put(name, value.map { BuildConfigField(type = type, value = it, comment = null) })
	}
}
