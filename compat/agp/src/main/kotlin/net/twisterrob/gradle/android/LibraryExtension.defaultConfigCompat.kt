package net.twisterrob.gradle.android

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.LibraryDefaultConfig
import com.android.build.api.dsl.LibraryExtension
import net.twisterrob.gradle.common.AGPVersions

/**
 * Prior to AGP 9.0 [LibraryExtension.defaultConfig] bound to
 * `CommonExtension<..., LibraryDefaultConfig, ...>.defaultConfig : DefaultConfigT`.
 * From AGP 9.0 [LibraryExtension.defaultConfig]: [LibraryDefaultConfig]
 * covariantly overrides [CommonExtension.defaultConfig]: [DefaultConfig].
 * Compiling against the latest results in:
 * > java.lang.NoSuchMethodError:
 * > 'com.android.build.api.dsl.LibraryDefaultConfig com.android.build.api.dsl.LibraryExtension.getDefaultConfig()'
 */
val LibraryExtension.defaultConfigCompat: LibraryDefaultConfig
	get() = when {
		AGPVersions.v9xx <= AGPVersions.CLASSPATH -> this.defaultConfig
		else -> (this as CommonExtension).defaultConfig as LibraryDefaultConfig
	}
