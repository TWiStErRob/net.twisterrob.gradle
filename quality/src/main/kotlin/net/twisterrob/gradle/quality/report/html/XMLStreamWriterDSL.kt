@file:Suppress("NOTHING_TO_INLINE")

package net.twisterrob.gradle.quality.report.html

import java.io.Writer
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

fun Writer.xmlWriter(): XMLStreamWriter =
	XMLOutputFactory
		.newInstance()
		.apply {
			// AGP 7.1.1 pulls in Dokka which pulls in woodstox, so let's configure it to match test style.
			// https://github.com/FasterXML/woodstox#configuration
			// WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL
			// Enable to consistently use double quotes for all attributes.
			// Discovered via ViolationsRendererTest.
			safeSetProperty("com.ctc.wstx.useDoubleQuotesInXmlDecl", true)
			// WstxOutputProperties.P_ADD_SPACE_AFTER_EMPTY_ELEM
			// Enable to make the output nicer looking.
			// Discovered via ViolationsRendererTest.
			safeSetProperty("com.ctc.wstx.addSpaceAfterEmptyElem", true)
			// org.codehaus.stax2.XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS
			// Disable to do writeStartElement/writeEndElement as called, no implicit writeEmptyElement.
			// Discovered via ViolationsRendererTest.
			safeSetProperty("org.codehaus.stax2.automaticEmptyElements", false)
			// WstxOutputProperties.P_OUTPUT_VALIDATE_CONTENT
			// Disable to allow writing hand-split <![CDATA[ ... ]]> blocks. See [escapeForCData].
			// Discovered via HtmlReportTaskTest.
			safeSetProperty("com.ctc.wstx.outputValidateContent", false)
		}
		.createXMLStreamWriter(this)

private fun XMLOutputFactory.safeSetProperty(name: String, value: Any?) {
	if (this.isPropertySupported(name)) {
		this.setProperty(name, value)
	}
}

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
