package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class DetailsViewModelTest_ANDROIDLINT {

	private val fixture = createAndroidLintFixture()

	@Test
	fun `message without escapes goes through as is`() {
		val model = DetailsViewModel(fixture.build<Violation>().apply {
			val lintMessage = """
				Title
				just a message
			""".trimIndent()
			setField("message", lintMessage)
		})

		val result = model.messaging.message

		assertEquals("""just a message""", result)
	}

	@Test
	fun `message with escapes gets escaped`() {
		val model = DetailsViewModel(fixture.build<Violation>().apply {
			// make sure message goes through the transformation
			setField("rule", "IconMissingDensityFolder")
			val lintMessage = """
				Title
				something with escapes:\n 1:\ 2:\\ 3:\\\ 4:\\\\
			""".trimIndent()
			setField("message", lintMessage)
		})

		val result = model.messaging.message

		assertEquals("""something with escapes:\\n 1:\\ 2:\\\\ 3:\\\\\\ 4:\\\\\\\\""", result)
	}

	@Test
	fun `HTML entities are kept`() {
		val model = DetailsViewModel(fixture.build<Violation>().apply {
			val input = """&, &amp;, `&amp;`, text: &#188;, code: `&#188;`"""
			val lintMessage = """
				Title: $input
				Message: $input
				Description: $input
			""".trimIndent()
			setField("message", lintMessage)
		})

		val result = model.messaging

		// The real expectation would be different, but that would require full syntactic parsing of lint's markdown.
		// See https://github.com/TWiStErRob/net.twisterrob.gradle/issues/65#issuecomment-860275509.
		assertEquals(
			"""Title: &amp;, &amp;amp;, `&amp;amp;`, text: &amp;#188;, code: `&amp;#188;`""",
			result.title
		)
		assertEquals(
			"""Message: &amp;, &amp;amp;, \`&amp;amp;\`, text: &amp;#188;, code: \`&amp;#188;\`""",
			result.message
		)
		assertEquals(
			"""Description: &amp;, &amp;amp;, \`&amp;amp;\`, text: &amp;#188;, code: \`&amp;#188;\`""",
			result.description
		)
	}

	companion object {

		private fun createAndroidLintFixture(): JFixture {
			return JFixture().apply {
				customise().lazyInstance(Project::class.java) {
					project(":" + build())
				}
				customise().lazyInstance(Task::class.java) { mock() }
				customise().intercept(Violation::class.java) {
					it.source.setField("reporter", "ANDROIDLINT")
				}
			}
		}
	}
}
