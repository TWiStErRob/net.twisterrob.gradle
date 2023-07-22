package net.twisterrob.gradle.build

import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.slf4j.ContextAwareTaskLogger

fun Task.silenceMessage(message: String) {
	(logger as ContextAwareTaskLogger)
		.setMessageRewriter(MessageSilencer(listOf(message)))
}

private class MessageSilencer(
	private val messageToSilence: List<String>
) : ContextAwareTaskLogger.MessageRewriter {

	override fun rewrite(logLevel: LogLevel, message: String): String? =
		if (message in messageToSilence) {
			null
		} else {
			message
		}
}
