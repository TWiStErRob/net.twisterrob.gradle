package net.twisterrob.gradle.quality.report.html

import com.android.annotations.VisibleForTesting
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violation.Location
import net.twisterrob.gradle.quality.Violations
import org.gradle.api.Project
import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.xml
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Base64
import java.util.zip.ZipFile
import kotlin.math.max
import kotlin.math.min

internal fun Project.produceXml(results: Grouper.Start<Violations>, xmlFile: File, xslFile: File) {
	val allViolations = results.list.flatMap { (it.violations ?: emptyList()) }
	val xml = xml("violations") {
		attribute("project", rootProject.name)
		allViolations
			.groupBy { it.category }
			.toSortedMap(nullsLast(compareBy { it }))
			.mapValues { (_, violations) ->
				violations
					.groupBy { it.source.reporter }
					.toSortedMap(compareBy { it })
					.mapValues { (_, violations) ->
						violations.sortedWith(
							compareBy<Violation> { it.rule }
								.thenBy { it.location.file }
								.thenBy { it.location.startLine }
								.thenBy { it.location.column }
						)
					}
			}
			.forEach { (category, categoryViolations) ->
				"category" {
					attribute("name", category ?: "unknown")
					categoryViolations.forEach { (reporter, reporterViolations) ->
						"reporter" {
							attribute("name", reporter)
							reporterViolations.forEach {
								try {
									emitViolation(it)
								} catch (ex: Throwable) {
									throw IllegalArgumentException(it.toString(), ex)
								}
							}
						}
					}
				}
			}
	}

	xmlFile.parentFile.mkdirs()
	xmlFile.delete()
	xmlFile.writeText(
		// append processing instructions here as the XML lib doesn't allow to do that
		"""
			|<?xml version="1.0" encoding="utf-8"?>
			|<?xml-stylesheet type="text/xsl" href="${xslFile.relativeTo(xmlFile.parentFile)}"?>
			|${xml.toString(prettyFormat = false)}
		""".trimMargin()
	)
}

private val binaryTypes = setOf("png", "webp", "jpg", "gif", "jar", "zip", "apk")

@VisibleForTesting
internal fun Node.emitViolation(v: Violation) {
	"violation" {
		with(v.location) {
			"location"{
				attribute("module", module.path)
				attribute("modulePrefix", module.path.substring(0, module.path.length - module.name.length - 1))
				attribute("moduleName", module.name)
				attribute("variant", variant)
				attribute("file", file.absolutePath)
				attribute("fileName", file.name)
				attribute("fileAbsoluteAsUrl", file.toURI())
				attribute("pathRelativeToProject", v.locationRelativeToProject)
				attribute("pathRelativeToModule", v.locationRelativeToModule)
				attribute("fileIsExternal", v.isLocationExternal)
				attribute("startLine", startLine)
				attribute("endLine", endLine)
				attribute("column", column)
			}
		}
		with(v.source) {
			"source" {
				attribute("parser", parser)
				attribute("source", source)
				attribute("reporter", reporter)
			}
		}
		"details"{
			attribute("rule", v.rule)
			getSuppression(v)?.let { attribute("suppress", escapeAttr(it)) }
			attribute("category", v.category ?: "unknown")
			attribute("severity", v.severity)
			when (v.source.reporter) {
				"ANDROIDLINT" -> {
					"title" { cdata(v.message.lineSequence().first()) }
					"message" {
						val messageLine = v.message.lineSequence().drop(1).first()
						val message = when {
							v.rule == "IconMissingDensityFolder" ->
								messageLine.replace(Regex("""(?<=Missing density variation folders in `)(.*?)(?=`:)""")) {
									it.value.replace("""\\""", """\""")
								}
							else -> messageLine
						}
						cdata(message.escapeMarkdownForJSTemplate())
					}
					"description" { cdata(v.message.lineSequence().drop(2).joinToString("\n").escapeMarkdownForJSTemplate()) }
				}

				else -> {
					if (v.message.count { it == '\n' } >= 1) {
						"description" { cdata(v.message) }
					} else {
						"message" { cdata(v.message) }
					}
				}
			}
			"context" {
				if (v.location.startLine != 0 && v.location.file.extension !in binaryTypes) {
					val (context, contextStart, contextEnd) = getContext(v)
					attribute("type", "code")
					attribute("language", v.location.language)
					attribute("startLine", contextStart)
					attribute("endLine", contextEnd)
					cdata(context.escapeCodeForJSTemplate())
				} else if (v.location.file.extension in binaryTypes) {
					when (v.location.file.extension) {
						"png", "gif", "jpg", "bmp", "webp" -> {
							val data = Base64.getEncoder().encodeToString(v.location.file.readBytes())
							attribute("type", "image")
							cdata("data:image/${v.location.file.extension};base64,${data}")
						}

						"jar", "zip", "apk" -> {
							attribute("type", "archive")
							try {
								val entries = ZipFile(v.location.file).entries().asSequence()
								cdata(entries.map { it.name }.sorted().joinToString("\n"))
							} catch (ex: Throwable) {
								cdata(PrintWriter(StringWriter()).apply { use { ex.printStackTrace(it) } }.toString())
							}
						}
					}
				} else if (v.location.file.isDirectory) {
					attribute("type", "archive")
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
					cdata(dir.parentFile.name + ":\n" + relevantListing)
				} else {
					attribute("type", "none")
				}
			}
		}
		"specific" {
			attribute("key", v.specifics.keys)
			attribute("value", v.specifics.values)
		}
	}
}

@VisibleForTesting
internal fun getContext(v: Violation): Triple<String, Int, Int> {
	val loc = v.location
	val lines = loc.file.readLines()
	val numContextLines = 2
	val contextStart = max(1, loc.startLine - numContextLines)
	val contextEnd = min(lines.size, loc.endLine + numContextLines)
	// Note: lines in list are counted from 0, but in file are counted from 1
	val contextLines = lines.subList(contextStart - 1, contextEnd)
	val context = contextLines.joinToString(System.lineSeparator())
	return Triple(context, contextStart, contextEnd)
}

private fun String.escapeCodeForJSTemplate(): String = this
	.replace("""\""", """\\""")
	.replace("""$""", """\$""")
	.replace("""`""", """\`""")

private fun String.escapeMarkdownForJSTemplate(): String = this
	.replace("""\""", """\\""")
	.replace("""$""", """\$""")
	.replace("""`""", """\`""")
	.replace("""&#xA;""", "\n")

private val Location.language: String
	get() = when (file.extension) {
		"kt" -> "kotlin"
		else -> file.extension
	}

private val Violation.locationRelativeToProject: String
	get() = location.module.rootProject.relativePath(location.file.parentFile) + File.separator

private val Violation.locationRelativeToModule: String
	get() = location.module.relativePath(location.file.parentFile) + File.separator

private val Violation.isLocationExternal: Boolean
	get() = locationRelativeToProject.startsWith("..")

private fun escapeAttr(value: String): String = StringBuilder().apply {
	for (c in value) {
		append(
			when (c) {
				//'"' -> "&quot;" // done by lib
				//'\'' -> "&apos;" // done by lib
				'\n' -> "&#xA;"
				'\r' -> "&#xD;"
				'<' -> "&lt;"
				'>' -> "&gt;"
				else -> c
			}
		)
	}
}.toString()

private fun getSuppression(v: Violation): String? =
	when (v.source.reporter) {
		"ANDROIDLINT" -> {
			when (v.location.file.extension) {
				"java" -> """@SuppressLint("${v.rule}") // TODO explanation"""
				"kt" -> """@SuppressLint("${v.rule}") // TODO explanation"""
				"xml" -> """tools:ignore="${v.rule}""""
				"gradle" -> """//noinspection ${v.rule} TODO explanation"""
				else -> """
					|<issue id="${v.rule}" severity="ignore">
					|    <!-- TODO explanation -->
					|    <ignore path="${if (v.isLocationExternal) v.location.file.name else v.locationRelativeToModule}" />
					|</issue>
				""".trimMargin()
			}
		}

		else -> null
	}
