package net.twisterrob.gradle.android

import com.android.build.api.dsl.AndroidSourceDirectorySet

fun AndroidSourceDirectorySet.srcDirCompat(srcDir: String) {
	this.directories.add(srcDir)
}
