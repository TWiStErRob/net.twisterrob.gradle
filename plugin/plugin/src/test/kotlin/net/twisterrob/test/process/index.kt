package net.twisterrob.test.process

import java.io.File
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.test.assertEquals

internal fun String.normalize() =
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

internal fun assertOutput(command: List<Any>, expected: String) {
	val output = command.map(Any?::toString).runCommand()
	assertEquals(expected.normalize(), output.normalize())
}
