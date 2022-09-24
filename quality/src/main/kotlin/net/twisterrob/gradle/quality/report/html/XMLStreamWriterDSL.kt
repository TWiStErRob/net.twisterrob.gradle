@file:Suppress(
	"NOTHING_TO_INLINE", // Keep inlines for consistency.
	"TooManyFunctions", // This file defines a full DSL framework.
)

package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.quality.report.bestXMLOutputFactory
import java.io.Writer
import javax.xml.stream.XMLStreamWriter

fun Writer.xmlWriter(): XMLStreamWriter =
	bestXMLOutputFactory()
		.createXMLStreamWriter(this)

fun XMLStreamWriter.use(block: (XMLStreamWriter) -> Unit) {
	val writer = this@use
	AutoCloseable {
		writer.flush()
		writer.close()
	}.use { block(writer) }
}

/**
 * Based on the amazing idea from https://www.schibsted.pl/blog/back-end/readable-xml-kotlin-extensions/
 *
 * @param version the XML version. Defaults to `1.0`.
 * @param encoding the XML encoding. Be sure to set the underlying Writer's encoding to the same.
 * @param content scope to write the XML content.
 */
inline fun XMLStreamWriter.document(
	version: String = "1.0",
	encoding: String = "utf-8",
	crossinline content: XMLStreamWriter.() -> Unit
): XMLStreamWriter =
	apply {
		writeStartDocument(encoding, version)
		content()
		writeEndDocument()
	}

inline fun XMLStreamWriter.element(
	name: String,
	crossinline content: XMLStreamWriter.() -> Unit
): XMLStreamWriter =
	apply {
		writeStartElement(name)
		content()
		writeEndElement()
	}

@Suppress("CanBeNonNullable") // TODEL https://github.com/detekt/detekt/issues/5331
inline fun <T : Any> XMLStreamWriter.optionalElement(
	name: String,
	value: T?,
	crossinline content: XMLStreamWriter.(T) -> Unit
) {
	value?.let { element(name) { content(it) } }
}

inline fun XMLStreamWriter.element(name: String, content: String) {
	element(name) {
		writeCharacters(content)
	}
}

inline fun XMLStreamWriter.element(name: String, content: Any) {
	element(name) {
		writeCharacters(content.toString())
	}
}

inline fun XMLStreamWriter.attribute(name: String, value: String) {
	writeAttribute(name, value)
}

inline fun XMLStreamWriter.attribute(name: String, value: Any) {
	writeAttribute(name, value.toString())
}

@Suppress("CanBeNonNullable") // TODEL https://github.com/detekt/detekt/issues/5331
inline fun <T : Any> XMLStreamWriter.optionalAttribute(
	name: String,
	value: T?,
	transformation: (T) -> String
) {
	value?.let { attribute(name, transformation(it)) }
}

inline fun XMLStreamWriter.cdata(content: String) {
	writeCData(content.escapeForCData())
}

inline fun XMLStreamWriter.cdata(content: Any) {
	writeCData(content.toString().escapeForCData())
}

fun String.escapeForCData(): String {
	val cdataEnd = """]]>"""
	val cdataStart = """<![CDATA["""
	return this
		// split cdataEnd into two pieces so XML parser doesn't recognize it
		.replace(cdataEnd, """]]${cdataEnd}${cdataStart}>""")
}
