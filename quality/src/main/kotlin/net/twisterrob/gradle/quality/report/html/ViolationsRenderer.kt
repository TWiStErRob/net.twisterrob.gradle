package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.ArchiveContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.CodeContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.DirectoryContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.EmptyContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.ErrorContext
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel.ImageContext
import net.twisterrob.gradle.quality.report.html.model.ViolationViewModel
import org.jetbrains.annotations.VisibleForTesting
import javax.xml.stream.XMLStreamWriter

internal fun renderXml(
	to: XMLStreamWriter,
	from: Map<Category?, Map<Reporter, List<Violation>>>,
	projectName: String,
	xslPath: String? = null
) {
	to.document {
		if (xslPath != null) {
			writeProcessingInstruction("xml-stylesheet", """type="text/xsl" href="${xslPath}"""")
		}
		element("violations") {
			attribute("project", projectName)
			from.forEach { (category, categoryViolations) ->
				element("category") {
					attribute("name", category ?: "unknown")
					categoryViolations.forEach { (reporter, reporterViolations) ->
						element("reporter") {
							attribute("name", reporter)
							reporterViolations.forEach { violation ->
								try {
									renderViolation(to, ViolationViewModel.create(violation))
								} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Throwable) {
									// Intentionally catch all exceptions,
									// because we want to crash the whole report, but with more information.
									throw IllegalArgumentException(violation.toString(), ex)
								}
							}
						}
					}
				}
			}
		}
	}
}

@VisibleForTesting
internal fun renderViolation(to: XMLStreamWriter, vm: ViolationViewModel) {
	to.element("violation") {
		with(vm.location) {
			element("location") {
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
			element("source") {
				attribute("parser", parser)
				attribute("source", source)
				attribute("reporter", reporter)
			}
		}
		with(vm.details) {
			element("details") {
				attribute("rule", rule)
				optionalAttribute("suppress", suppression) { it }
				attribute("category", category)
				attribute("severity", severity)
				optionalAttribute("documentation", documentation) { it.toASCIIString() }
				optionalElement("title", messaging.title) { cdata(it) }
				optionalElement("message", messaging.message) { cdata(it) }
				optionalElement("description", messaging.description) { cdata(it) }
				element("context") renderContext@{
					try {
						// Make sure context is ready and only then start rendering it.
						// This is a peculiarity of streaming XML rendering,
						// because there's no backtrack once the element started outputting.
						context.resolve()
					} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Throwable) {
						// Intentionally catch all exceptions, because we don't want to crash the whole report.
						render(to, ErrorContext(context, ex))
						return@renderContext
					}
					// Outside try-catch, because if there's an error in rendering, that should crash the report.
					render(to, context)
				}
			}
		}
		vm.specifics.forEach { (k, v) ->
			element("specific") {
				attribute("key", k)
				attribute("value", v)
			}
		}
	}
}

private fun render(to: XMLStreamWriter, context: ContextViewModel) {
	with(to) {
		when (context) {
			is EmptyContext -> {
				attribute("type", "none")
			}

			is ErrorContext -> {
				attribute("type", "error")
				attribute("message", context.message)
				cdata(context.fullStackTrace)
			}

			is CodeContext -> {
				attribute("type", "code")
				attribute("language", context.language)
				attribute("startLine", context.startLine)
				attribute("endLine", context.endLine)
				cdata(context.data)
			}

			is DirectoryContext -> {
				attribute("type", "archive")
				cdata(context.listing)
			}

			is ArchiveContext -> {
				attribute("type", "archive")
				cdata(context.listing)
			}

			is ImageContext -> {
				attribute("type", "image")
				cdata(context.embeddedPixels)
			}
		}
	}
}
