@file:Suppress("NOTHING_TO_INLINE")

package net.twisterrob.gradle.quality.report.html

import java.io.Writer
import java.lang.management.ManagementFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import kotlin.reflect.full.staticFunctions

private fun bestXMLOutputFactory(): XMLOutputFactory =
	if (ManagementFactory.getRuntimeMXBean().specVersion == "1.8") {
		// Useful link: https://stackoverflow.com/questions/11314604/how-to-set-saxon-as-the-xslt-processor-in-java
		XMLOutputFactory.newInstance()
			.apply @Suppress("KDocUnresolvedReference") {
				// AGP 7.1.1 pulls in Dokka which pulls in woodstox, so let's configure it to match test style.
				// https://github.com/FasterXML/woodstox#configuration
				/**
				 * Enable to consistently use double quotes for all attributes.
				 * @see com.ctc.wstx.api.WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL
				 * @since Discovered via ViolationsRendererTest.
				 */
				safeSetProperty("com.ctc.wstx.useDoubleQuotesInXmlDecl", true)
				/**
				 * Enable to make the output nicer looking.
				 * @see com.ctc.wstx.api.WstxOutputProperties.P_ADD_SPACE_AFTER_EMPTY_ELEM
				 * @since Discovered via ViolationsRendererTest.
				 */
				safeSetProperty("com.ctc.wstx.addSpaceAfterEmptyElem", true)
				/**
				 * Disable to do writeStartElement/writeEndElement as called, no implicit writeEmptyElement.
				 * @see org.codehaus.stax2.XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS
				 * @since Discovered via ViolationsRendererTest.
				 */
				safeSetProperty("org.codehaus.stax2.automaticEmptyElements", false)
				/**
				 * Disable to allow writing hand-split <![CDATA[ ... ]]> blocks. See [escapeForCData].
				 * @see com.ctc.wstx.api.WstxOutputProperties.P_OUTPUT_VALIDATE_CONTENT
				 * @since Discovered via HtmlReportTaskTest.
				 */
				safeSetProperty("com.ctc.wstx.outputValidateContent", false)
			}
	} else { // >= 9, because running Java 7 is simply not supported for any Gradle / AGP combination.
		// XMLOutputFactory.newDefaultFactory() is @since Java 9.
		XMLOutputFactory::class.staticFunctions.single { it.name == "newDefaultFactory" }.call() as XMLOutputFactory
	}

fun Writer.xmlWriter(): XMLStreamWriter =
	bestXMLOutputFactory()
		.also { println("XMLAPI::xmlWriter.factory = ${it}") }
		.createXMLStreamWriter(this)
		.also { println("XMLAPI::xmlWriter() = ${it}") }

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
