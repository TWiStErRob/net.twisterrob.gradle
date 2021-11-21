package net.twisterrob.gradle.internal.android

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByName
import java.io.Serializable

fun Project.addBuildConfigField40x(name: String, type: String, value: Provider<out Serializable>) {
	val android: BaseExtension = this.extensions.getByName<BaseExtension>("android")
	android.defaultConfig.buildConfigField(type, name, value.get().toString())
}
