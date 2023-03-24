package net.twisterrob.gradle.android

import com.android.build.api.variant.AndroidTest
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.HasAndroidTest
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.androidTestCompat70x
import net.twisterrob.gradle.internal.android.androidTestCompat71x

/**
 * Compatibility version for [ApplicationVariant.androidTest] DSL:
 * `androidComponents.onVariant { it.androidTest }`.
 *
 * * AGP 7.0.x introduced [ApplicationVariant.androidTest].
 * * AGP 7.1.x moved [ApplicationVariant.androidTest] to [HasAndroidTest.androidTest].
 */
val ApplicationVariant.androidTestCompat: AndroidTest?
	get() =
		@Suppress("MISSING_DEPENDENCY_SUPERCLASS") // Will exist when necessary.
		when {
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.androidTestCompat71x
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.androidTestCompat70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
