package net.twisterrob.gradle.quality.report.html

import com.android.annotations.VisibleForTesting
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.ArchiveContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.CodeContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.DirectoryContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.EmptyContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.ErrorContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.ImageContext
import net.twisterrob.gradle.quality.report.html.model.ViolationViewModel
import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.xml

internal fun renderXml(groups: Map<Category, Map<Reporter, List<Violation>>>): Node =
	xml("violations") {
		groups.forEach { (category, categoryViolations) ->
			"category" {
				attribute("name", category ?: "unknown")
				categoryViolations.forEach { (reporter, reporterViolations) ->
					"reporter" {
						attribute("name", reporter)
						reporterViolations.forEach {
							try {
								renderViolation(ViolationViewModel.create(it))
							} catch (ex: Throwable) {
								throw IllegalArgumentException(it.toString(), ex)
							}
						}
					}
				}
			}
		}
	}

@VisibleForTesting
internal fun Node.renderViolation(vm: ViolationViewModel) {
	"violation" {
		with(vm.location) {
			"location"{
				attribute("module", modulePath)
				attribute("modulePrefix", modulePrefix)
				attribute("moduleName", moduleName)
				attribute("variant", variant)
				attribute("file", file)
				attribute("fileName", fileName)
				attribute("fileAbsoluteAsUrl", fileAbsoluteAsUrl)
				attribute("pathRelativeToProject", locationRelativeToProject)
				attribute("pathRelativeToModule", locationRelativeToModule)
				attribute("fileIsExternal", isLocationExternal)
				attribute("startLine", startLine)
				attribute("endLine", endLine)
				attribute("column", column)
			}
		}
		with(vm.source) {
			"source" {
				attribute("parser", parser)
				attribute("source", source)
				attribute("reporter", reporter)
			}
		}
		with(vm.details) {
			"details"{
				attribute("rule", rule)
				suppression?.let { attribute("suppress", escapeAttr(it)) }
				attribute("category", category)
				attribute("severity", severity)
				messaging.title?.let { "title" { cdataSafe(it) } }
				messaging.message?.let { "message" { cdataSafe(it) } }
				messaging.description?.let { "description" { cdataSafe(it) } }
				"context" {
					try {
						render(context)
					} catch (ex: Throwable) {
						this.attributes.clear()
						this.children.filterIsInstance<Node>().forEach { removeNode(it) }
						render(ErrorContext(context, ex))
					}
				}
			}
		}
		vm.specifics.forEach { k, v ->
			"specific" {
				attribute("key", k)
				attribute("value", v)
			}
		}
	}
}

private fun Node.render(context: ContextViewModel) {
	when (context) {
		is EmptyContext -> {
			attribute("type", "none")
		}

		is ErrorContext -> {
			attribute("type", "error")
			attribute("message", context.message)
			cdataSafe(context.fullStackTrace)
		}

		is CodeContext -> {
			attribute("type", "code")
			attribute("language", context.language)
			attribute("startLine", context.startLine)
			attribute("endLine", context.endLine)
			cdataSafe(context.data)
		}

		is DirectoryContext -> {
			attribute("type", "archive")
			cdataSafe(context.listing)
		}

		is ArchiveContext -> {
			attribute("type", "archive")
			cdataSafe(context.listing)
		}

		is ImageContext -> {
			attribute("type", "image")
			cdata(context.embeddedPixels)
		}
	}
}

private fun String.escapeForCData(): String {
	val cdataEnd = """]]>"""
	val cdataStart = """<![CDATA["""
	return this
		// split cdataEnd into two pieces so XML parser doesn't recognize it
		.replace(cdataEnd, """]]${cdataEnd}${cdataStart}>""")
}

//TODEL https://github.com/redundent/kotlin-xml-builder/pull/13
private fun Node.cdataSafe(text: String) = cdata(text.escapeForCData())

private fun escapeAttr(value: String): String = StringBuilder().apply {
	for (c in value) {
		append(
			when (c) {
				//'"' -> "&quot;" // done by lib
				//'\'' -> "&apos;" // done by lib
				// REPORT escaping full XML data in attribute
				'\n' -> "&#xA;"
				'\r' -> "&#xD;"
				'<' -> "&lt;"
				'>' -> "&gt;"
				else -> c
			}
		)
	}
}.toString()
