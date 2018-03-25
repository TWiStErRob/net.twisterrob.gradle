package net.twisterrob.gradle.test

import java.io.StringWriter
import java.io.Writer

/**
 * Gradle's test listener captures output and then pushes an extra new line,
 * resulting in a gap between every line doubling the output size.
 *
 * To prevent this wait for at least one line before pushing downstream.
 *
 * @see org.gradle.api.internal.tasks.testing.logging.TestEventLogger.onOutput
 */
internal class WriteOnlyWhenLineCompleteWriter(private val delegate: Writer) : Writer() {

	companion object {
		private const val MAX_LENGTH = 1 * 1024 * 1024
	}

	private val buf = StringWriter()

	override fun write(cbuf: CharArray?, off: Int, len: Int) {
		buf.write(cbuf, off, len)
		flush()
	}

	override fun flush() {
		if (buf.buffer.endsWith(System.lineSeparator()) || buf.buffer.length > MAX_LENGTH) {
			doFlush()
		}
	}

	override fun close() {
		doFlush() // forced flush, there may not have been an EOL
		delegate.close()
	}

	/**
	 * Actually send data to delegate in a single event.
	 */
	private fun doFlush() {
		delegate.write(buf.toString())
		delegate.flush()
		buf.buffer.setLength(0) // reset buffer it was used up
	}
}
