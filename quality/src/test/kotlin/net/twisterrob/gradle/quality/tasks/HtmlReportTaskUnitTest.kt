package net.twisterrob.gradle.quality.tasks

import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

class HtmlReportTaskUnitTest {

	@Test fun `transformation error displays affected file names`() {
		val project = ProjectBuilder.builder().build()
		project.plugins.apply("reporting-base")
		val sut = project.tasks.create("sut", HtmlReportTask::class.java)

		val ex = assertThrows<Throwable> {
			sut.transform()
		}

		assertThat(ex.message, containsString(sut.xml.get().toString()))
		assertThat(ex.message, containsString(sut.xslOutput.get().toString()))
		assertThat(ex.message, containsString(sut.html.get().toString()))
		assertNotNull(ex.cause)
	}
}
