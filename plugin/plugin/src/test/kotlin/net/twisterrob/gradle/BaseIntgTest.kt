package net.twisterrob.gradle

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleTestKitDirRelocator
import org.junit.Rule
import org.junit.jupiter.api.BeforeAll

abstract class BaseIntgTest {

	@get:Rule(order = 1) val testKit = GradleTestKitDirRelocator()
	@get:Rule(order = 2) val gradle = GradleRunnerRule().apply {
		clearAfterFailure = false
	}

	companion object {

		// TODEL once the GradleRunnerRule is updated to not use assert()
		@BeforeAll @JvmStatic fun enableAssertions() {
			val kotlinClassLoader = Class.forName("kotlin._Assertions").classLoader!!
			kotlinClassLoader.setClassAssertionStatus("kotlin._Assertions", true)
		}
	}
}
