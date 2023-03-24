package com.android.build.gradle.internal.test.report

@Suppress("EXPOSED_RECEIVER_TYPE") // It can be only used if someone sees TestResult anyway.
fun TestResult.ignoredCompat74x(device: String, project: String, flavorName: String) {
	this.ignored(device, project, flavorName)
}
