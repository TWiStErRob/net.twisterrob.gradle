package net.twisterrob.gradle.logging

import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyNoInteractions
import org.slf4j.Logger

class RedirectingLoggerFactoryTest {
	@Test fun `no redirect for logger`() {
		val factory = RedirectingLoggerFactory({ null })

		val logger = factory.getLogger("foo")

		assertThat(logger, defaultLogger())
	}

	@Test fun `redirect exists for logger`() {
		val redirect: LogLevelRedirect = mock()
		val factory = RedirectingLoggerFactory({ redirect })

		val logger = factory.getLogger("foo")

		assertThat(logger, myLogger())
		verifyNoInteractions(redirect)
	}

	companion object {
		private fun myLogger(): Matcher<Logger> =
			instanceOf(GradleLogLevelRedirectingLogger::class.java)

		private fun defaultLogger(): Matcher<Logger> =
			not(instanceOf(GradleLogLevelRedirectingLogger::class.java))
	}
}
