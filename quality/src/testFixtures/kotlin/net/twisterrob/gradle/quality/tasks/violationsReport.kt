package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.projectFile
import java.io.File

fun GradleRunnerRule.violationsReport(extension: String): File =
	this.projectFile("build/reports/violations.${extension}")
