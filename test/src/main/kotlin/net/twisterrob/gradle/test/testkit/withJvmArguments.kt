package net.twisterrob.gradle.test.testkit

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.DefaultGradleRunner

fun GradleRunner.withJvmArguments(vararg args: String) {
	(this as DefaultGradleRunner).withJvmArguments(*args)
}

fun GradleRunner.withJvmArguments(args: List<String>) {
	(this as DefaultGradleRunner).withJvmArguments(args)
}
