@file:Suppress("NOTHING_TO_INLINE")

package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.quality.report.bestXMLOutputFactory
import java.io.Writer
import javax.xml.stream.XMLStreamWriter

fun Writer.xmlWriter(): XMLStreamWriter =
	bestXMLOutputFactory()
		.also { println("XMLAPI::xmlWriter.factory = ${it}") }
		.createXMLStreamWriter(this)
		.also { println("XMLAPI::xmlWriter() = ${it}") }

fun XMLStreamWriter.use(block: (XMLStreamWriter) -> Unit) {
	AutoCloseable {
		this@use.flush()
		this@use.close()
	}.use { block(this@use) }
}

/**
 * Based on the amazing idea from https://www.schibsted.pl/blog/back-end/readable-xml-kotlin-extensions/
 * @param encoding be sure to set the underlying Writer's encoding to the same
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
