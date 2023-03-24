package net.twisterrob.gradle.android

import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.internal.component.ConsumableCreationConfig
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.isMinifyEnabledCompat70x
import net.twisterrob.gradle.internal.android.isMinifyEnabledCompat74x
import org.gradle.api.Incubating

/**
 * Compatibility version for [ApplicationVariant.isMinifyEnabled] DSL:
 * `androidComponents.onVariant { it.isMinifyEnabled }`.
 *
 * * AGP 4.x had minification on the DSL.
 * * AGP 7.0.x didn't expose internal [ConsumableCreationConfig.minifiedEnabled].
 * * AGP 7.1.x didn't expose internal [ConsumableCreationConfig.minifiedEnabled].
 * * AGP 7.2.x didn't expose internal [ConsumableCreationConfig.minifiedEnabled].
 * * AGP 7.3.x didn't expose internal [ConsumableCreationConfig.minifiedEnabled].
 * * AGP 7.4.x introduced [ApplicationVariant.isMinifyEnabled].
 */
@get:Incubating
val ApplicationVariant.isMinifyEnabledCompat: Boolean
	get() =
		@Suppress("MISSING_DEPENDENCY_SUPERCLASS") // Will exist when necessary.
		when {
			AGPVersions.v74x <= AGPVersions.CLASSPATH -> this.isMinifyEnabledCompat74x
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.isMinifyEnabledCompat70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
