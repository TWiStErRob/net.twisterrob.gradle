package net.twisterrob.gradle.android

import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import net.twisterrob.gradle.common.AGPVersions

/**
 * Prior to AGP 9.0 [ApplicationExtension.defaultConfig] bound to
 * `CommonExtension<..., ApplicationDefaultConfig, ...>.defaultConfig : DefaultConfigT`.
 * From AGP 9.0 [ApplicationExtension.defaultConfig]: [ApplicationDefaultConfig]
 * covariantly overrides [CommonExtension.defaultConfig]: [DefaultConfig].
 * Compiling against the latest results in:
 * > java.lang.NoSuchMethodError:
 * > 'com.android.build.api.dsl.ApplicationDefaultConfig com.android.build.api.dsl.ApplicationExtension.getDefaultConfig()'
 */
val ApplicationExtension.defaultConfigCompat: ApplicationDefaultConfig
	get() = when {
		AGPVersions.v9xx <= AGPVersions.CLASSPATH -> this.defaultConfig
		else -> (this as CommonExtension).defaultConfig as ApplicationDefaultConfig
	}
