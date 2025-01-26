package net.twisterrob.gradle.common

import net.twisterrob.gradle.test.Project
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class TaskCreationConfigurationTest {

	@Test fun `registering a task preConfigures, but does not create it`() {
		val mockConfiguration: TaskCreationConfiguration<TestTask> = mock()
		val fixtProject = Project()
		val createdTasks: Map<String, Task> = fixtProject.collectCreatedTasks()

		val provider = fixtProject.registerTask("testTask", mockConfiguration)

		assertThat(fixtProject.tasks.names, hasItem("testTask"))
		verify(mockConfiguration).preConfigure(fixtProject, provider)
		assertThat(createdTasks.keys, not(hasItem(":testTask")))
		verifyNoMoreInteractions(mockConfiguration)
	}

	@Test fun `registering a task and get-ing it creates and configures`() {
		val mockConfiguration: TaskCreationConfiguration<TestTask> = mock()
		val fixtProject = Project()
		val createdTasks: Map<String, Task> = fixtProject.collectCreatedTasks()

		val provider = fixtProject.registerTask("testTask", mockConfiguration)
		@Suppress("EagerGradleConfiguration") // Explicitly trigger configuration.
		val task = provider.get()

		assertThat(fixtProject.tasks.names, hasItem("testTask"))
		inOrder(mockConfiguration) {
			verify(mockConfiguration).preConfigure(fixtProject, provider)
			verify(mockConfiguration).configure(task)
		}
		assertThat(createdTasks, hasEntry(":testTask", task))
		verifyNoMoreInteractions(mockConfiguration)
	}

	@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
	internal abstract class TestTask : DefaultTask()
}

private fun Project.collectCreatedTasks(): Map<String, Task> {
	val createdTasks: MutableMap<String, Task> = mutableMapOf()
	this.tasks.configureEach { createdTasks[it.path] = it }
	return createdTasks
}
