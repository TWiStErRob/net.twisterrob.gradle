package net.twisterrob.gradle.test.testkit

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.DefaultGradleRunner

fun GradleRunner.withJvmArguments(vararg args: String) {
	@Suppress("MemberExtensionConflict") // It's fine, that's the point.
	(this as DefaultGradleRunner).withJvmArguments(*args)
}

fun GradleRunner.withJvmArguments(args: List<String>) {
	@Suppress("MemberExtensionConflict") // It's fine, that's the point.
	(this as DefaultGradleRunner).withJvmArguments(args)
}
