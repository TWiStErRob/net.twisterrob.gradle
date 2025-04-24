package net.twisterrob.gradle.quality.tasks

import net.twisterrob.gradle.test.Project
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HtmlReportTaskUnitTest {

	@Test fun `transformation error displays affected file names`() {
		val project = Project()
		project.plugins.apply("reporting-base")
		@Suppress("EagerGradleConfiguration") // Explicitly trigger configuration so task can be tested.
		val sut = project.tasks.register("sut", HtmlReportTask::class.java).get()

		val ex = assertThrows<Throwable> {
			sut.transform()
		}

		assertThat(ex.message, containsString(sut.xml.get().toString()))
		assertThat(ex.message, containsString(sut.xsl.get().toString()))
		assertThat(ex.message, containsString(sut.html.get().toString()))
		assertNotNull(ex.cause)
	}
}
