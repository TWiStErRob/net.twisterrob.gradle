package net.twisterrob.gradle

import net.twisterrob.gradle.test.GradleRunnerRule
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName

abstract class BaseIntgTest {

	@Rule @JvmField val gradle = GradleRunnerRule(false)
	@Rule @JvmField val testName = TestName()

	companion object {
		// TODEL once the GradleRunnerRule is updated to not use assert()
		@BeforeClass @JvmStatic fun enableAssertions() {
			val kotlinClassLoader = Class.forName("kotlin._Assertions").classLoader!!
			kotlinClassLoader.setClassAssertionStatus("kotlin._Assertions", true)
		}
	}
}
