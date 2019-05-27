package net.twisterrob.gradle

import net.twisterrob.gradle.test.GradleRunnerRule
import org.junit.BeforeClass
import org.junit.Rule

abstract class BaseIntgTest {

	@get:Rule val gradle = GradleRunnerRule(false)

	companion object {
		// TODEL once the GradleRunnerRule is updated to not use assert()
		@BeforeClass @JvmStatic fun enableAssertions() {
			val kotlinClassLoader = Class.forName("kotlin._Assertions").classLoader!!
			kotlinClassLoader.setClassAssertionStatus("kotlin._Assertions", true)
		}
	}
}
