package net.twisterrob.gradle.android

import com.android.build.api.dsl.Lint
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.fatalCompat70x
import net.twisterrob.gradle.internal.android.fatalCompat71x

/**
 * Compatibility version for [Lint.fatal] DSL:
 * `android.lint.fatal("LintId")`.
 *
 * * AGP 7.0.x introduced [Lint.fatal] as a function.
 * * AGP 7.1.x changed [Lint.fatal] to be a property with a [MutableSet] type.
 */
fun Lint.fatalCompat(id: String) {
	when {
		AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.fatalCompat71x(id)
		AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.fatalCompat70x(id)
		else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
	}
}
