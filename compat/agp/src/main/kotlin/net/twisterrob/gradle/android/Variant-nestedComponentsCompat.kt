package net.twisterrob.gradle.android

import com.android.build.api.variant.Component
import com.android.build.api.variant.Variant
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.nestedComponentsCompat70x
import net.twisterrob.gradle.internal.android.nestedComponentsCompat71x
import org.gradle.api.Incubating

/**
 * Compatibility version for [Variant.nestedComponents] DSL:
 * `androidComponents.onVariant { it.nestedComponents }`.
 *
 * * AGP 7.0.x introduced [Component]s, but didn't expose a list on [Variant].
 * * AGP 7.1.x introduced [Variant.nestedComponents].
 */
@get:Incubating
val Variant.nestedComponentsCompat: List<Component>
	get() =
		@Suppress("MISSING_DEPENDENCY_SUPERCLASS") // Will exist when necessary.
		when {
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.nestedComponentsCompat71x
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.nestedComponentsCompat70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
