package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.common.listFilesInDirectory
import net.twisterrob.gradle.quality.Violation
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Base64
import java.util.zip.ZipFile
import kotlin.math.max
import kotlin.math.min

sealed class ContextViewModel {

	/**
	 * Make sure that any external dependencies are resolved and lazy properties calculated.
	 * After returning from this function, a [ContextViewModel] is considered ready for consumption
	 * and not expected to fail for any reason.
	 */
	open fun resolve() {}

	object EmptyContext : ContextViewModel()

	class ErrorContext(
		@Suppress("UNUSED_PARAMETER") context: ContextViewModel,
		private val ex: Throwable
	) : ContextViewModel() {

		val message: String by lazy {
			val exceptions = generateSequence(ex) { it.cause }
			exceptions.joinToString(System.lineSeparator())
		}

		val fullStackTrace: String by lazy {
			StringWriter()
				.apply { PrintWriter(this, true).use { ex.printStackTrace(it) } }
				.toString()
		}
	}

	class CodeContext(private val v: Violation) : ContextViewModel() {

		private val context by lazy { getContext(v) }
		val type: String get() = "code"
		val language: String
			get() = when (v.location.file.extension) {
				"" -> "binary"
				"kt" -> "kotlin"
				"kts" -> "kotlin"
				else -> v.location.file.extension
			}
		val startLine: Int get() = context.second
		val endLine: Int get() = context.third
		val data: String get() = context.first.escapeCodeForJSTemplate()

		override fun resolve() {
			context
		}

		companion object {

			private fun String.escapeCodeForJSTemplate(): String =
				this
					.replace("""\""", """\\""")
					.replace("""$""", """\$""")
					.replace("""`""", """\`""")

			/**
			 * Get the code context of the lines that are flagged as failed.
			 *
			 * Tries to return the lines as requested + 2 lines before and after if possible.
			 * Invalid lines flagged will result in an error message with 0 to 0 resulting context.
			 */
			private fun getContext(v: Violation): Triple<String, Int, Int> {
				val loc = v.location
				val file = loc.file.absoluteFile
				val lines = file.readLines()
				fun invalidLocation(): Nothing =
					error(
						"Invalid location in ${file}: requested ${loc.startLine} to ${loc.endLine}, " +
								"but file only has lines 1 to ${lines.size}."
					)
				if (loc.endLine < loc.startLine) invalidLocation()
				val numContextLines = 2
				if (lines.size < loc.startLine) invalidLocation()
				if (loc.endLine < 0 || lines.size < loc.endLine) invalidLocation()
				if (lines.size < loc.endLine) invalidLocation()
				val contextStart = max(1, loc.startLine - numContextLines)
				val contextEnd = min(lines.size, loc.endLine + numContextLines)
				// Note: lines in list are counted from 0, but in file are counted from 1
				val contextLines = lines.subList(contextStart - 1, contextEnd)
				val context = contextLines.joinToString(System.lineSeparator())
				return Triple(context, contextStart, contextEnd)
			}
		}
	}

	class DirectoryContext(private val v: Violation) : ContextViewModel() {

		val listing: String by lazy {
			fun <E> Iterable<E>.replace(old: E, new: E): Iterable<E> =
				map { if (it == old) new else it }

			fun Array<File>.sorted(): Iterable<File> =
				sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name })

			val dir = v.location.file
			val contents = dir.listFilesInDirectory()
				.sorted()
				.joinToString("\n") { it.name }
				.prependIndent("\t")
			val dirWithContents = dir.name + ":\n" + contents
			val siblings = dir.parentFile.listFilesInDirectory().sorted().map { it.name }
			val relevantListing = siblings
				.replace(dir.name, dirWithContents)
				.joinToString("\n")
				.prependIndent("\t")
			dir.parentFile.name + ":\n" + relevantListing
		}

		override fun resolve() {
			listing
		}
	}

	class ImageContext(private val v: Violation) : ContextViewModel() {

		val embeddedPixels: String by lazy {
			val data = Base64.getEncoder().encodeToString(v.location.file.readBytes())
			"data:image/${v.location.file.extension};base64,${data}"
		}

		override fun resolve() {
			embeddedPixels
		}
	}

	class ArchiveContext(private val v: Violation) : ContextViewModel() {

		val listing: String by lazy {
			val entries = ZipFile(v.location.file).use { it.entries().toList() }
			entries.map { it.name }.sorted().joinToString("\n")
		}

		override fun resolve() {
			listing
		}
	}

	companion object {

		fun create(v: Violation): ContextViewModel =
			when {
				v.location.file.extension in setOf("png", "gif", "jpg", "bmp", "webp") -> ImageContext(v)
				v.location.file.extension in setOf("jar", "zip", "apk") -> ArchiveContext(v)
				v.location.file.isDirectory -> DirectoryContext(v)
				v.location.startLine != 0 -> CodeContext(v)
				else -> EmptyContext
			}
	}
}
