package net.twisterrob.gradle.android

import com.android.build.api.dsl.Lint
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.checkAllWarningsCompat70x
import net.twisterrob.gradle.internal.android.checkAllWarningsCompat71x

/**
 * Compatibility version for [Lint.checkAllWarnings] DSL:
 * `android.lint.checkAllWarnings`.
 *
 * * AGP 7.0.x introduced [Lint.isCheckAllWarnings].
 * * AGP 7.1.x renamed [Lint.isCheckAllWarnings] to [Lint.checkAllWarnings].
 */
@Suppress("BooleanPropertyNaming") // Following AGP's naming.
var Lint.checkAllWarningsCompat: Boolean
	get() =
		when {
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.checkAllWarningsCompat71x
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.checkAllWarningsCompat70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
	set(value) {
		when {
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.checkAllWarningsCompat71x = value
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.checkAllWarningsCompat70x = value
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
	}
