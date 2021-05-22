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
	fun `IconMissingDensityFolder specific message escapes are removed`() {
		val model = DetailsViewModel(fixture.build<Violation>().apply {
			setField("rule", "IconMissingDensityFolder")
			val lintMessage = """
				Title
				Missing density variation folders in `src\\main\\res`: drawable-hdpi
			""".trimIndent()
			setField("message", lintMessage)
		})

		val result = model.messaging.message

		assertEquals("""Missing density variation folders in \`src\\main\\res\`: drawable-hdpi""", result)
	}

	class Suppressions {

		private val fixture = createAndroidLintFixture()

		@Test
		fun `unknown files are suppressed with lint-xml`() {
			val model = DetailsViewModel(fixture.build<Violation>().apply {
				// this.location.file is fixture'd
			})

			val result = model.suppression

			assertThat(result, containsString("""<issue id="${model.rule}" severity="ignore">"""))
		}

		@Test
		fun `java files are suppressed with annotation`() {
			val model = DetailsViewModel(fixture.build<Violation>().apply {
				this.location.setField("file", File(fixture.build<String>() + ".java"))
			})

			val result = model.suppression

			assertThat(result, containsString("""@SuppressLint("${model.rule}")"""))
		}
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
