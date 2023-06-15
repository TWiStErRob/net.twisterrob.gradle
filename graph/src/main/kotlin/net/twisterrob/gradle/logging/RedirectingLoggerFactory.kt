package net.twisterrob.gradle.logging

import org.gradle.api.logging.Logging
import org.slf4j.ILoggerFactory
import org.slf4j.Logger

internal class RedirectingLoggerFactory(
	private val redirects: (String) -> LogLevelRedirect?,
	private val factory: ILoggerFactory = ILoggerFactory { name -> Logging.getLogger(name) }
) : ILoggerFactory {
	override fun getLogger(name: String): Logger {
		val logger = factory.getLogger(name)
		val redirect = redirects(name) ?: return logger
		return GradleLogLevelRedirectingLogger(logger, redirect)
	}
}
