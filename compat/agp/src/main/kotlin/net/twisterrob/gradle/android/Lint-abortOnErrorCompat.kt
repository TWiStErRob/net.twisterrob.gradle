package net.twisterrob.gradle.android

import com.android.build.api.dsl.Lint
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.abortOnErrorCompat70x
import net.twisterrob.gradle.internal.android.abortOnErrorCompat71x
import org.gradle.api.Incubating

/**
 * Compatibility version for [Lint.abortOnError] DSL:
 * `android.lint.abortOnError`.
 *
 * * AGP 7.0.x introduced [Lint.isAbortOnError].
 * * AGP 7.1.x renamed [Lint.isAbortOnError] to [Lint.abortOnError].
 */
@get:Incubating
@set:Incubating
@Suppress("BooleanPropertyNaming") // Following AGP's naming.
var Lint.abortOnErrorCompat: Boolean
	get() =
		when {
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.abortOnErrorCompat71x
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.abortOnErrorCompat70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
	set(value) {
		when {
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.abortOnErrorCompat71x = value
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.abortOnErrorCompat70x = value
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
	}
