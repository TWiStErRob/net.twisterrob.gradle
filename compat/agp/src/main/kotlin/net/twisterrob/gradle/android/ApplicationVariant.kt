package net.twisterrob.gradle.android

import com.android.build.api.variant.AndroidTest
import com.android.build.api.variant.ApplicationVariant
import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.internal.android.androidTest70x
import net.twisterrob.gradle.internal.android.androidTest71x

val ApplicationVariant.androidTestCompat: AndroidTest?
	get() =
		when {
			AGPVersions.v71x <= AGPVersions.CLASSPATH -> this.androidTest71x
			AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.androidTest70x
			else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
		}
