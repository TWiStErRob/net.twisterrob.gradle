package com.android.build.gradle.internal.test.report

import net.twisterrob.gradle.common.AGPVersions

@Suppress("EXPOSED_RECEIVER_TYPE") // It can be only used if someone sees TestResult anyway.
fun TestResult.ignoredCompat(device: String, project: String, flavorName: String) {
	when {
		AGPVersions.v74x <= AGPVersions.CLASSPATH -> this.ignoredCompat74x(device, project, flavorName)
		AGPVersions.v70x <= AGPVersions.CLASSPATH -> this.ignoredCompat40x()
		else -> AGPVersions.olderThan7NotSupported(AGPVersions.CLASSPATH)
	}
}
