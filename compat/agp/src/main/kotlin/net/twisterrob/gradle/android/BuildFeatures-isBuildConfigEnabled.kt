package net.twisterrob.gradle.android

import com.android.build.api.dsl.BuildFeatures
import net.twisterrob.gradle.common.AGPVersions

/**
 * Compatibility property to access [BuildFeatures.buildConfig].
 * There has been no API changes, but the default value changed, therefore `null` changed meaning.
 *
 * * AGP 4.x introduced [BuildFeatures] with a default [BuildFeatures.buildConfig] value of `true`.
 * * AGP 7.x kept the default [BuildFeatures.buildConfig] value of `true`. So `null` means `true`.
 * * AGP 8.x changed the default [BuildFeatures.buildConfig] value to `false`. So `null` means `false`.
 */
val BuildFeatures.isBuildConfigEnabled: Boolean
	get() =
		when {
			AGPVersions.v80x <= AGPVersions.CLASSPATH -> this.buildConfig == true
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.buildConfig != false
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
