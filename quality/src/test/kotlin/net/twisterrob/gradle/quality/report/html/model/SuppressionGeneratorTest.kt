package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import org.gradle.api.Project
import org.gradle.api.Task
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.io.File

class SuppressionGeneratorTest {

	private val fixture = createAndroidLintFixture()

	private val sut = SuppressionGenerator()

	@Test
	fun `unknown files are suppressed with lint-xml`() {
		val input = fixture.build<Violation>().apply {
			// this.location.file is fixture'd
		}

		val output = sut.getSuppression(input)

		assertThat(output, containsString("""<issue id="${input.rule}" severity="ignore">"""))
	}

	@Test
	fun `java files are suppressed with annotation`() {
		val input = fixture.build<Violation>().apply {
			this.location.setField("file", File(fixture.build<String>() + ".java"))
		}

		val output = sut.getSuppression(input)

		assertThat(output, containsString("""@SuppressLint("${input.rule}")"""))
	}

	@Test
	fun `kotlin files are suppressed with annotation`() {
		val input = fixture.build<Violation>().apply {
			this.location.setField("file", File(fixture.build<String>() + ".kt"))
		}

		val output = sut.getSuppression(input)

		assertThat(output, containsString("""@SuppressLint("${input.rule}")"""))
	}

	companion object {

		private fun createAndroidLintFixture(): JFixture {
			return JFixture().apply {
				customise().lazyInstance(Project::class.java) {
					mockProject(buildProjectPath())
				}
				customise().lazyInstance(Task::class.java) { mock() }
				customise().intercept(Violation::class.java) {
					it.source.setField("reporter", "ANDROIDLINT")
				}
			}
		}
	}
}
