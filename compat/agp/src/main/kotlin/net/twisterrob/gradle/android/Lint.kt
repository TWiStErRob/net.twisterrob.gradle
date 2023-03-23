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
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.abortOnError
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.isAbortOnError70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
	set(value) {
		when {
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.abortOnError = value
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.isAbortOnError70x = value
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
	}
