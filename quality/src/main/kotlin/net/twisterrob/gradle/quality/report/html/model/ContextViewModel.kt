package net.twisterrob.gradle.quality.report.html.model

import net.twisterrob.gradle.quality.Violation
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Base64
import java.util.zip.ZipFile
import kotlin.math.max
import kotlin.math.min

sealed class ContextViewModel {

	object EmptyContext : ContextViewModel()

	class CodeContext(private val v: Violation) : ContextViewModel() {
		private val context by lazy { getContext(v) }
		val type: String get() = "code"
		val language: String
			get() = when (v.location.file.extension) {
				"kt" -> "kotlin"
				else -> v.location.file.extension
			}
		val startLine: Int get() = context.second
		val endLine: Int get() = context.third
		val data: String get() = context.first.escapeCodeForJSTemplate()

		companion object {

			private fun String.escapeCodeForJSTemplate(): String = this
				.replace("""\""", """\\""")
				.replace("""$""", """\$""")
				.replace("""`""", """\`""")

			private fun getContext(v: Violation): Triple<String, Int, Int> {
				fun invalid(message: String) = Triple(message, 0, 0)
				val loc = v.location
				val file = loc.file.absoluteFile
				val lines = try {
					file.readLines()
				} catch (ex: IOException) {
					val exceptions = generateSequence<Throwable>(ex) { it.cause }
					return invalid(exceptions.joinToString(System.lineSeparator()))
				}
				fun invalidLocation() = invalid(
					"Invalid location in ${file}: requested ${loc.startLine} to ${loc.endLine}, " +
							"but file only has lines 1 to ${lines.size}."
				)
				if (loc.endLine < loc.startLine) return invalidLocation()
				val numContextLines = 2
				val contextStart = max(1, loc.startLine - numContextLines)
				val contextEnd = min(lines.size, loc.endLine + numContextLines)
				if (lines.size < contextStart) return invalidLocation()
				if (contextEnd < 0) return invalidLocation()
				if (lines.size < contextEnd) return invalidLocation()
				// Note: lines in list are counted from 0, but in file are counted from 1
				val contextLines = lines.subList(contextStart - 1, contextEnd)
				val context = contextLines.joinToString(System.lineSeparator())
				return Triple(context, contextStart, contextEnd)
			}
		}
	}

	class DirectoryContext(private val v: Violation) : ContextViewModel() {
		val listing by lazy {
			fun <E> Iterable<E>.replace(old: E, new: E) = map { if (it == old) new else it }
			fun Array<File>.sorted() = sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name })
			val dir = v.location.file
			val contents = dir.listFiles()
				.sorted()
				.joinToString("\n") { it.name }
				.prependIndent("\t")
			val dirWithContents = dir.name + ":\n" + contents
			val siblings = dir.parentFile.listFiles().sorted().map { it.name }
			val relevantListing = siblings
				.replace(dir.name, dirWithContents)
				.joinToString("\n")
				.prependIndent("\t")
			dir.parentFile.name + ":\n" + relevantListing
		}
	}

	class ImageContext(private val v: Violation) : ContextViewModel() {
		val embeddedPixels by lazy {
			val data = Base64.getEncoder().encodeToString(v.location.file.readBytes())
			"data:image/${v.location.file.extension};base64,${data}"
		}
	}

	class ArchiveContext(private val v: Violation) : ContextViewModel() {
		val listing by lazy {
			try {
				val entries = ZipFile(v.location.file).entries().asSequence()
				entries.map { it.name }.sorted().joinToString("\n")
			} catch (ex: Throwable) {
				PrintWriter(StringWriter()).apply { use { ex.printStackTrace(it) } }.toString()
			}
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
