package net.twisterrob.gradle.android

import com.android.build.api.dsl.Lint
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.isAbortOnError70x
import org.gradle.api.Incubating

/**
 * Compatibility version for `android.lint.abortOnError` DSL.
 * AGP 7.0.x introduced [Lint.isAbortOnError].
 * AGP 7.1.x renamed [Lint.isAbortOnError] to [Lint.abortOnError].
 */
@get:Incubating
@set:Incubating
var Lint.isAbortOnErrorCompat: Boolean
	get() =
		when {
			AGPVersions.CLASSPATH >= AGPVersions.v71x -> this.abortOnError
			AGPVersions.CLASSPATH compatible AGPVersions.v70x -> this.isAbortOnError70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
	set(value) {
		when {
			AGPVersions.CLASSPATH >= AGPVersions.v71x -> this.abortOnError = value
			AGPVersions.CLASSPATH compatible AGPVersions.v70x -> this.isAbortOnError70x = value
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
	}
