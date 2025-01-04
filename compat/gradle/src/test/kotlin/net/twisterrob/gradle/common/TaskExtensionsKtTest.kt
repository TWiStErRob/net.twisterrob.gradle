package net.twisterrob.gradle.common

import net.twisterrob.gradle.test.Project
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TaskExtensionsKtTest {
	private val project = Project()

	@Test fun `task was launched explicitly`() {
		val task = project.tasks.register("myTask").get()
		project.gradle.startParameter.setTaskNames(listOf(":myTask"))

		assertTrue(task.wasLaunchedExplicitly)
		assertTrue(task.wasLaunchedOnly)
	}

	@Test fun `task provider was launched explicitly`() {
		var isConfigured = false
		val task = project.tasks.register("myTask") { isConfigured = true }
		project.gradle.startParameter.setTaskNames(listOf(":myTask"))

		assertTrue(task.wasLaunchedExplicitly(project))
		assertTrue(task.wasLaunchedOnly(project))
		assertFalse(isConfigured)
	}

	@Test fun `task was launched without qualification`() {
		val task = project.tasks.register("myTask").get()
		project.gradle.startParameter.setTaskNames(listOf("myTask"))

		assertFalse(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}

	@Test fun `task provider was launched without qualification`() {
		var isConfigured = false
		val task = project.tasks.register("myTask") { isConfigured = true }
		project.gradle.startParameter.setTaskNames(listOf("myTask"))

		assertFalse(task.wasLaunchedExplicitly(project))
		assertFalse(task.wasLaunchedOnly(project))
		assertFalse(isConfigured)
	}

	@Test fun `task was launched explicitly among others`() {
		val task = project.tasks.register("myTask").get()
		project.gradle.startParameter.setTaskNames(listOf("otherTask", ":myTask", "someTask"))

		assertTrue(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}

	@Test fun `task provider was launched explicitly among others`() {
		var isConfigured = false
		val task = project.tasks.register("myTask") { isConfigured = true }
		project.gradle.startParameter.setTaskNames(listOf("otherTask", ":myTask", "someTask"))

		assertTrue(task.wasLaunchedExplicitly(project))
		assertFalse(task.wasLaunchedOnly(project))
		assertFalse(isConfigured)
	}

	@Test fun `task was launched explicitly and not`() {
		val task = project.tasks.register("myTask").get()
		project.gradle.startParameter.setTaskNames(listOf("myTask", ":myTask"))

		assertTrue(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}

	@Test fun `task provider was launched explicitly and not`() {
		var isConfigured = false
		val task = project.tasks.register("myTask") { isConfigured = true }
		project.gradle.startParameter.setTaskNames(listOf("myTask", ":myTask"))

		assertTrue(task.wasLaunchedExplicitly(project))
		assertFalse(task.wasLaunchedOnly(project))
		assertFalse(isConfigured)
	}

	@Test fun `task was not launched explicitly`() {
		val task = project.tasks.register("myTask").get()
		project.gradle.startParameter.setTaskNames(emptyList())

		assertFalse(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}

	@Test fun `task provider was not launched explicitly`() {
		var isConfigured = false
		val task = project.tasks.register("myTask") { isConfigured = true }
		project.gradle.startParameter.setTaskNames(emptyList())

		assertFalse(task.wasLaunchedExplicitly(project))
		assertFalse(task.wasLaunchedOnly(project))
		assertFalse(isConfigured)
	}

	@Test fun `task was not launched explicitly, but others were`() {
		val task = project.tasks.register("myTask").get()
		project.gradle.startParameter.setTaskNames(listOf("otherTask", "someTask"))

		assertFalse(task.wasLaunchedExplicitly)
		assertFalse(task.wasLaunchedOnly)
	}

	@Test fun `task provider was not launched explicitly, but others were`() {
		var isConfigured = false
		val task = project.tasks.register("myTask") { isConfigured = true }
		project.gradle.startParameter.setTaskNames(listOf("otherTask", "someTask"))

		assertFalse(task.wasLaunchedExplicitly(project))
		assertFalse(task.wasLaunchedOnly(project))
		assertFalse(isConfigured)
	}
}
