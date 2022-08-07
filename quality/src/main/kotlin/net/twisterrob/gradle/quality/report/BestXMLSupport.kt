package net.twisterrob.gradle.quality.report

import java.lang.management.ManagementFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.transform.TransformerFactory
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.isAccessible

/**
 * We're aiming to get the default Java writer:
 * [com.sun.xml.internal.stream.XMLOutputFactoryImpl]
 * creating [com.sun.xml.internal.stream.writers.XMLStreamWriterImpl],
 * but without hardcoding it.
 *
 * Strategy:
 *  * Call newDefaultFactory whenever available.
 *  * If it doesn't exist, but the class has a declared default implementation, use that.
 *  * Fall back to hardcoding, which might fail anyway if the default is not there.
 *  * Give up, and use whatever is available.
 */
internal fun bestXMLOutputFactory(): XMLOutputFactory {
	// >= 9, because running Java 7 is simply not supported for any Gradle / AGP combination.
	if (ManagementFactory.getRuntimeMXBean().specVersion != "1.8") {
		// XMLOutputFactory.newDefaultFactory() is @since Java 9.
		return XMLOutputFactory::class.staticFunctions
			.first { it.name == "newDefaultFactory" && it.parameters.isEmpty() }
			.call() as XMLOutputFactory
	}

	fun String.isClassLoadable(): Boolean =
		try {
			Class.forName(this)
			true
		} catch (e: ClassNotFoundException) {
			false
		}

	val defaultImpl =
		XMLOutputFactory::class.declaredMembers
			.singleOrNull { it.name == "DEFAULIMPL" }
			?.apply { isAccessible = true }
			?.call() as String?
			?: "com.sun.xml.internal.stream.XMLOutputFactoryImpl"
	if (defaultImpl.isClassLoadable()) {
		// return XMLOutputFactory.newFactory(defaultImpl, null as ClassLoader?) is @since Java 6,
		// BUT see build.gradle.kts too.
		val newFactory = XMLOutputFactory::class.staticFunctions
			.firstOrNull { it.name == "newFactory" && it.parameters.size == 2 }
		if (newFactory != null) {
			/**
			 * This is very silly, instead of providing the class name directly, we have to provide a System property.
			 */
			val factoryId = ::bestXMLOutputFactory.name + ".temporary.xml.output.factory"
			System.setProperty(factoryId, defaultImpl)
			try {
				return newFactory.call(factoryId, null) as XMLOutputFactory
			} finally {
				System.clearProperty(factoryId)
			}
		}
	}
	// Useful link: https://stackoverflow.com/questions/11314604/how-to-set-saxon-as-the-xslt-processor-in-java
	return XMLOutputFactory.newInstance()
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
}

private fun XMLOutputFactory.safeSetProperty(name: String, value: Any?) {
	if (this.isPropertySupported(name)) {
		this.setProperty(name, value)
	}
}
/**
 * We're aiming to get the default Java writer:
 * [com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl]
 * creating [com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl],
 * but without hardcoding it.
 *
 * Strategy:
 *  * Call [TransformerFactory.newDefaultInstance] whenever available.
 *  * Fall back to hardcoding, which might fail anyway if the default is not there.
 *  * Give up, and use whatever is available.
 */
internal fun bestXMLTransformerFactory(): TransformerFactory {
	// >= Java 9, because running Java 7 is simply not supported for any Gradle / AGP combination.
	if (ManagementFactory.getRuntimeMXBean().specVersion != "1.8") {
		// return TransformerFactory.newDefaultInstance() // which is @since Java 9.
		return TransformerFactory::class.staticFunctions
			.first { it.name == "newDefaultInstance" && it.parameters.isEmpty() }
			.call() as TransformerFactory
	}

	fun String.isClassLoadable(): Boolean =
		try {
			Class.forName(this)
			true
		} catch (e: ClassNotFoundException) {
			false
		}

	val defaultImpl = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"
	if (defaultImpl.isClassLoadable()) {
		return TransformerFactory.newInstance(defaultImpl, null as ClassLoader?)
	}
	// Useful link: https://stackoverflow.com/questions/11314604/how-to-set-saxon-as-the-xslt-processor-in-java
	return TransformerFactory.newInstance()
}
