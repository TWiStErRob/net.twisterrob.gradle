package net.twisterrob.test.process

import net.twisterrob.test.withRootCause
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES

internal fun String.normalize(): String =
	trim().replace("\r?\n".toRegex(), System.lineSeparator())

fun Iterable<String>.runCommand(
	workingDir: File = File("."),
	timeout: Long = MINUTES.toMillis(60),
	verifyError: Boolean = true
): String {
	val commandString = this.joinToString(separator = " ") {
		if (it.contains(" ")) """"$it"""" else it
	}
	@Suppress("detekt.ForbiddenMethodCall") // TODO revise println() and make them log somehow correctly!
	println("Running in ${workingDir.absolutePath}:\n$commandString")
	return ProcessBuilder(this.toList())
		.directory(workingDir)
		.redirectOutput(ProcessBuilder.Redirect.PIPE)
		.redirectError(ProcessBuilder.Redirect.PIPE)
		.start()
		.apply { waitFor(timeout, MILLISECONDS) }
		.also { process ->
			if (verifyError && process.exitValue() != 0) {
				val out = process.inputStream.bufferedReader().readText()
				val err = process.errorStream.bufferedReader().readText()
				assertEquals(
					0, process.exitValue(),
					@Suppress("detekt.StringShouldBeRawString") // It would be more complex that way.
					"Non-zero exit value:\nstdout:\n${out}\nstderr:\n${err}"
				)
			}
		}
		.run { inputStream.bufferedReader().readText() }
}

internal fun assertOutput(command: List<Any>, expected: String, message: String? = null) {
	try {
		val output = command.map(Any?::toString).runCommand()
		assertEquals(expected.normalize(), output.normalize(), message)
	} catch (@Suppress("detekt.SwallowedException") ex: Throwable) { // Detekt doesn't see into the extension fun.
		throw ex.withRootCause(IllegalArgumentException("Command: $command"))
	}
}
