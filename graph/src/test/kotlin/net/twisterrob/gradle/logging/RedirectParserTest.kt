package net.twisterrob.gradle.logging

import org.gradle.api.logging.LogLevel
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.hasSize
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Properties

class RedirectParserTest {

	@Nested
	inner class `edge cases` {
		@Test fun `empty properties are empty`() {
			val input = Properties()

			val redirects = RedirectParser().parse(input)

			assertThat(redirects.entries, empty())
		}

		@Test fun `random properties are ignored`() {
			val input = Properties(
				"""
					foo=bar
					foo.bar=baz
					foo.bar.baz=qux
				""".trimIndent()
			)

			val redirects = RedirectParser().parse(input)

			assertThat(redirects.entries, empty())
		}

		@Test fun `single mapping`() {
			val input = Properties(
				"""
					net.twisterrob.gradle.logging.redirect.my.package=DEBUG->INFO
				""".trimIndent()
			)

			val redirects = RedirectParser().parse(input)

			assertThat(redirects.entries, hasSize(1))
			assertThat(redirects.keys.single(), equalTo("my.package"))
		}

		@Test fun `multiple mapping`() {
			val input = Properties(
				"""
					net.twisterrob.gradle.logging.redirect.my.pack.age=*->INFO
					net.twisterrob.gradle.logging.redirect.other.packg=*->WARN
				""".trimIndent()
			)

			val redirects = RedirectParser().parse(input)

			assertThat(redirects, allOf(hasKey("my.pack.age"), hasKey("other.packg")))
			assertThat(redirects.entries, hasSize(2))
			assertThat(redirects.getValue("my.pack.age")(LogLevel.QUIET), equalTo(LogLevel.INFO))
			assertThat(redirects.getValue("other.packg")(LogLevel.LIFECYCLE), equalTo(LogLevel.WARN))
		}

		@Test fun `freeform mapping`() {
			val mapper = testMapping(" DEBUG  ->WARN   ,     INFO->     ERROR ")

			LOG_LEVELS.forEach { level ->
				@Suppress("ElseCaseInsteadOfExhaustiveWhen") // else is really else in this case.
				val expectedLevel = when (level) {
					LogLevel.DEBUG -> LogLevel.WARN
					LogLevel.INFO -> LogLevel.ERROR
					else -> level
				}
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}

		@Test fun `root package`() {
			val input = Properties(
				"""
					net.twisterrob.gradle.logging.redirect.=DEBUG->INFO
				""".trimIndent()
			)

			val redirects = RedirectParser().parse(input)

			assertThat(redirects.entries, hasSize(1))
			assertThat(redirects.keys.single(), equalTo(""))
		}
	}

	@Nested
	inner class `mapping tests` {

		@Test fun `glob mapping`() {
			val mapper = testMapping("*->ERROR")

			LOG_LEVELS.forEach { level ->
				val expectedLevel = LogLevel.ERROR
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}

		@Test fun `single change mapping`() {
			val mapper = testMapping("DEBUG->INFO")

			LOG_LEVELS.forEach { level ->
				val expectedLevel = if (level == LogLevel.DEBUG) LogLevel.INFO else level
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}

		@Test fun `multiple distinct change mapping`() {
			val mapper = testMapping("DEBUG->WARN,INFO->ERROR")

			LOG_LEVELS.forEach { level ->
				@Suppress("ElseCaseInsteadOfExhaustiveWhen") // else is really else in this case.
				val expectedLevel = when (level) {
					LogLevel.DEBUG -> LogLevel.WARN
					LogLevel.INFO -> LogLevel.ERROR
					else -> level
				}
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}

		@Test fun `multiple change mapping`() {
			val mapper = testMapping("DEBUG->WARN,INFO->WARN")

			LOG_LEVELS.forEach { level ->
				val expectedLevel = if (level == LogLevel.DEBUG || level == LogLevel.INFO) LogLevel.WARN else level
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}

		@Test fun `switcheroo mapping`() {
			val mapper = testMapping("WARN->ERROR,ERROR->WARN")

			LOG_LEVELS.forEach { level ->
				@Suppress("ElseCaseInsteadOfExhaustiveWhen") // else is really else in this case.
				val expectedLevel = when (level) {
					LogLevel.WARN -> LogLevel.ERROR
					LogLevel.ERROR -> LogLevel.WARN
					else -> level
				}
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}

		@Test fun `contradictory mapping`() {
			val mapper = testMapping("DEBUG->WARN,DEBUG->ERROR")

			LOG_LEVELS.forEach { level ->
				val expectedLevel = if (level == LogLevel.DEBUG) LogLevel.ERROR else level
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}

		@Test fun `elevate to Gradle level`() {
			val mapper = testMapping("DEBUG->QUIET")

			LOG_LEVELS.forEach { level ->
				val expectedLevel = if (level == LogLevel.DEBUG) LogLevel.QUIET else level
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}

		@Test fun `elevate from trace to Gradle level`() {
			val mapper = testMapping("TRACE->QUIET")

			LOG_LEVELS.forEach { level ->
				val expectedLevel = if (level == TRACE) LogLevel.QUIET else level
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}

		@Test fun `hide via trace`() {
			val mapper = testMapping("INFO->TRACE")

			LOG_LEVELS.forEach { level ->
				val expectedLevel = if (level == LogLevel.INFO) TRACE else level
				assertThat(mapper(level), equalTo(expectedLevel))
			}
		}
	}

	@Nested
	inner class `error cases` {
		@Test fun `missing destination`() {
			assertThrows<IllegalArgumentException> {
				testMapping("DEBUG->")
			}
		}

		@Test fun `invalid destination`() {
			assertThrows<IllegalArgumentException> {
				testMapping("DEBUG->INVALID")
			}
		}

		@Test fun `missing source`() {
			assertThrows<IllegalArgumentException> {
				testMapping("->DEBUG")
			}
		}

		@Test fun `invalid source`() {
			assertThrows<IllegalArgumentException> {
				testMapping("INVALID->DEBUG")
			}
		}

		@Test fun `missing both`() {
			assertThrows<IllegalArgumentException> {
				testMapping("->")
			}
		}

		@Test fun `wrong delimiter`() {
			assertThrows<IllegalArgumentException> {
				testMapping("DEBUG=>INFO")
			}
		}
	}

	private fun testMapping(mapping: String): LogLevelRedirect {
		val input = Properties(
			"""
				net.twisterrob.gradle.logging.redirect.test=${mapping}
			""".trimIndent()
		)

		val redirects = RedirectParser().parse(input)

		assertThat(redirects.entries, hasSize(1))
		assertThat(redirects.keys.single(), equalTo("test"))
		return redirects.values.single()
	}

	companion object {
		@Suppress("UNCHECKED_CAST")
		val LOG_LEVELS: Array<LogLevel?> = LogLevel.values() as Array<LogLevel?> + null

		val TRACE: LogLevel? = null
	}
}

@Suppress("TestFunctionName") // Simulate constructor.
private fun Properties(@Language("properties") str: String): Properties =
	Properties().apply { str.reader().use { load(it) } }
