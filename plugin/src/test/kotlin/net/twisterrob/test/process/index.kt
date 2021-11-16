package net.twisterrob.test.process

import org.junit.jupiter.api.Assertions.assertEquals
import org.opentest4j.AssertionFailedError
import java.io.File
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES

internal fun String.normalize(): String =
	trim().replace("\r?\n".toRegex(), System.lineSeparator())

internal fun Iterable<String>.runCommand(
	workingDir: File = File("."),
	timeout: Long = MINUTES.toMillis(60)
): String {
	val commandString = this.joinToString(separator = " ") {
		if (it.contains(" ")) """"$it"""" else it
	}
	println("Running in ${workingDir.absolutePath}:\n$commandString")
	return ProcessBuilder(this.toList())
		.directory(workingDir)
		.redirectOutput(ProcessBuilder.Redirect.PIPE)
		.redirectError(ProcessBuilder.Redirect.PIPE)
		.start()
		.apply { waitFor(timeout, MILLISECONDS) }
		.run { inputStream.bufferedReader().readText() }
}

internal fun assertOutput(command: List<Any>, expected: String, message: String? = null) {
	try {
		val output = command.map(Any?::toString).runCommand()
		assertEquals(expected.normalize(), output.normalize(), message)
	} catch (ex: Throwable) {
		val err = generateSequence(ex) { it.cause }.last()
		val cause = IllegalArgumentException("Command: $command")
		if (err is AssertionFailedError) {
			// Workaround for https://github.com/ota4j-team/opentest4j/issues/5#issuecomment-940474063.
			AssertionFailedError(err.message, err.expected, err.actual, cause)
		} else {
			err.initCause(cause)
		}
		throw ex
	}
}
