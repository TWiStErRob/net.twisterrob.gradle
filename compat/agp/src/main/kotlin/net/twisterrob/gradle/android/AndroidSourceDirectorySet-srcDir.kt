package net.twisterrob.gradle.android

import com.android.build.api.dsl.AndroidSourceDirectorySet
import net.twisterrob.gradle.common.AGPVersions

fun AndroidSourceDirectorySet.srcDirCompat(srcDir: String) {
	when {
		AGPVersions.v85x <= AGPVersions.CLASSPATH -> this.directories.add(srcDir)
		AGPVersions.v81x <= AGPVersions.CLASSPATH -> @Suppress("DEPRECATION") this.srcDir(srcDir)
		else -> AGPVersions.olderThan81NotSupported(AGPVersions.CLASSPATH)
	}
}
