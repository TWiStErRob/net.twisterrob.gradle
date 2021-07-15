package net.twisterrob.gradle

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleTestKitDirRelocator
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GradleTestKitDirRelocator::class)
abstract class BaseIntgTest {

	protected abstract val gradle: GradleRunnerRule

	companion object {

		// TODEL once the GradleRunnerRule is updated to not use assert()
		@BeforeAll @JvmStatic fun enableAssertions() {
			val kotlinClassLoader = Class.forName("kotlin._Assertions").classLoader!!
			kotlinClassLoader.setClassAssertionStatus("kotlin._Assertions", true)
		}
	}
}
