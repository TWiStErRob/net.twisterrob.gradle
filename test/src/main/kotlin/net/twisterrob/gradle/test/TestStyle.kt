package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

inline fun GradleRunnerRule.runBuild(block: GradleRunnerRule.() -> GradleRunner): BuildResult =
	this.block().build()

inline fun GradleRunnerRule.runFailingBuild(block: GradleRunnerRule.() -> GradleRunner): BuildResult =
	this.block().buildAndFail()
