package net.twisterrob.gradle.dsl

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class ProjectExtensionsKtTest {

	@Test fun `reporting extension function returns the reporting extension`() {
		val mockProject: Project = mock()
		val mockExtensions: ExtensionContainer = mock()
		val mockReportingExtension: ReportingExtension = mock()
		whenever(mockProject.extensions).thenReturn(mockExtensions)
		whenever(mockExtensions.getByName("reporting")).thenReturn(mockReportingExtension)

		val actual = mockProject.reporting

		assertSame(mockReportingExtension, actual)
		verify(mockProject).extensions
		verify(mockExtensions).getByName("reporting")
		verifyNoInteractions(mockReportingExtension)
		verifyNoMoreInteractions(mockProject, mockExtensions)
	}

	@Test fun `reporting extension function does not swallow errors`() {
		val mockProject: Project = mock()
		val mockExtensions: ExtensionContainer = mock()
		whenever(mockProject.extensions).thenReturn(mockExtensions)
		whenever(mockExtensions.getByName("reporting")).thenThrow(UnknownDomainObjectException("test"))

		assertThrows<UnknownDomainObjectException>("test") {
			mockProject.reporting
		}

		verify(mockProject).extensions
		verify(mockExtensions).getByName("reporting")
		verifyNoMoreInteractions(mockProject, mockExtensions)
	}

	@Test fun `reporting extension function fails if wrong extension registered`() {
		val mockProject: Project = mock()
		val mockExtensions: ExtensionContainer = mock()
		whenever(mockProject.extensions).thenReturn(mockExtensions)
		whenever(mockExtensions.getByName("reporting")).thenReturn("definitely not a ReportingExtension")

		assertThrows<IllegalStateException> {
			mockProject.reporting
		}

		verify(mockProject).extensions
		verify(mockExtensions).getByName("reporting")
		verifyNoMoreInteractions(mockProject, mockExtensions)
	}

	@Test fun `TaskContainer contains extension function returns true if task exists`() {
		val mockTasks: TaskContainer = mock()
		val mockTask: TaskProvider<Task> = mock()
		whenever(mockTasks.named("task")).thenReturn(mockTask)

		val actual = mockTasks.contains("task")

		assertTrue(actual)
		verify(mockTasks).named("task")
		verifyNoInteractions(mockTask)
		verifyNoMoreInteractions(mockTasks)
	}

	@Test fun `TaskContainer contains extension function can be used as operator`() {
		val mockTasks: TaskContainer = mock()
		val mockTask: TaskProvider<Task> = mock()
		whenever(mockTasks.named("task")).thenReturn(mockTask)

		val actual = "task" in mockTasks

		assertTrue(actual)
		verify(mockTasks).named("task")
		verifyNoInteractions(mockTask)
		verifyNoMoreInteractions(mockTasks)
	}

	@Test fun `TaskContainer contains extension function returns false if task does not exist`() {
		val mockTasks: TaskContainer = mock()
		val mockTask: TaskProvider<Task> = mock()
		whenever(mockTasks.named("task")).thenThrow(UnknownTaskException("test"))

		val actual = mockTasks.contains("task")

		assertFalse(actual)
		verify(mockTasks).named("task")
		verifyNoInteractions(mockTask)
		verifyNoMoreInteractions(mockTasks)
	}

	@Test fun `TaskContainer contains extension function propagates other errors`() {
		val mockTasks: TaskContainer = mock()
		val mockTask: TaskProvider<Task> = mock()
		whenever(mockTasks.named("task")).thenThrow(UnknownDomainObjectException("test"))

		assertThrows<UnknownDomainObjectException> {
			mockTasks.contains("task")
		}

		verify(mockTasks).named("task")
		verifyNoInteractions(mockTask)
		verifyNoMoreInteractions(mockTasks)
	}
}
