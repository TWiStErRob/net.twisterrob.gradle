package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

inline fun GradleRunnerRule.runBuild(block: GradleRunnerRule.() -> GradleRunner): BuildResult =
	this.block().build().apply {
		// STOPSHIP
		assertNoOutputLine("Setting the namespace via a source AndroidManifest.xml's package attribute is deprecated.")
	}

inline fun GradleRunnerRule.runFailingBuild(block: GradleRunnerRule.() -> GradleRunner): BuildResult =
	this.block().buildAndFail()
