package net.twisterrob.gradle.common

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TaskExtensionsKtTest {
	private val project = ProjectBuilder.builder().build()

	@Test fun `task was launched explicitly`() {
		val task = project.tasks.create("myTask")
		project.gradle.startParameter.setTaskNames(listOf(":myTask"))

		assertTrue(task.wasLaunchedExplicitly)
		assertTrue(task.wasLaunchedOnly)
	}

	@Test fun `task was launched without qualification`() {
		val task = project.tasks.create("myTask")
		project.gradle.startParameter.setTaskNames(listOf("myTask"))

		assertFalse(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}

	@Test fun `task was launched explicitly among others`() {
		val task = project.tasks.create("myTask")
		project.gradle.startParameter.setTaskNames(listOf("otherTask", ":myTask", "someTask"))

		assertTrue(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}

	@Test fun `task was launched explicitly and not`() {
		val task = project.tasks.create("myTask")
		project.gradle.startParameter.setTaskNames(listOf("myTask", ":myTask"))

		assertTrue(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}

	@Test fun `task was not launched explicitly`() {
		val task = project.tasks.create("myTask")
		project.gradle.startParameter.setTaskNames(listOf())

		assertFalse(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}

	@Test fun `task was not launched explicitly, but others were`() {
		val task = project.tasks.create("myTask")
		project.gradle.startParameter.setTaskNames(listOf("otherTask", "someTask"))

		assertFalse(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}
}
