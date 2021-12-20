package net.twisterrob.gradle.dsl

import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.reporting.ReportingExtension
import org.junit.jupiter.api.Assertions.assertSame
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
}
