package net.twisterrob.gradle.android

import com.android.build.api.variant.Component
import com.android.build.api.variant.Variant
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.componentsCompat70x
import net.twisterrob.gradle.internal.android.componentsCompat71x
import net.twisterrob.gradle.internal.android.componentsCompat73x
import org.gradle.api.Incubating

/**
 * Compatibility version for [Variant.components] DSL:
 * `androidComponents.onVariant { it.components }`.
 *
 * * AGP 7.0.x introduced [Component]s, but didn't expose a list on [Variant].
 * * AGP 7.1.x introduced [Variant.nestedComponents], but not [Variant.components].
 * * AGP 7.3.x introduced [Variant.components] alongside [Variant.nestedComponents].
 */
@get:Incubating
val Variant.componentsCompat: List<Component>
	get() =
		@Suppress("MISSING_DEPENDENCY_SUPERCLASS") // Will exist when necessary.
		when {
			AGPVersions.v73x <= AGPVersions.CLASSPATH -> this.componentsCompat73x
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.componentsCompat71x
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.componentsCompat70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
