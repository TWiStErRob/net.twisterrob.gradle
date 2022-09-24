package net.twisterrob.gradle.quality.report

import java.lang.management.ManagementFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.transform.TransformerFactory
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

/**
 * We're aiming to get the default Java writer:
 * [com.sun.xml.internal.stream.XMLOutputFactoryImpl]
 * creating [com.sun.xml.internal.stream.writers.XMLStreamWriterImpl],
 * but without hardcoding it.
 *
 * Strategy:
 *  * Call [XMLOutputFactory.newDefaultFactory] whenever available.
 *  * If it doesn't exist, but the class has a declared default implementation, use that.
 *  * Fall back to hardcoding, which might fail anyway if the default is not there.
 *  * Give up, and use whatever is available.
 */
@Suppress("ReturnCount") // Open to suggestions.
internal fun bestXMLOutputFactory(): XMLOutputFactory {
	// >= 9, because running Java 7 is simply not supported for any Gradle / AGP combination.
	if (ManagementFactory.getRuntimeMXBean().specVersion != "1.8") {
		// XMLOutputFactory.newDefaultFactory() is @since Java 9.
		return XMLOutputFactory::class.staticFunctions
			.first { it.name == "newDefaultFactory" && it.parameters.isEmpty() }
			.call() as XMLOutputFactory
	}

	val defaultImpl =
		XMLOutputFactory::class.declaredMembers
			.singleOrNull { it.name == "DEFAULIMPL" }
			?.let { field ->
				field.isAccessible = true
				field.call() as String
			}
			?: "com.sun.xml.internal.stream.XMLOutputFactoryImpl"
	if (defaultImpl.isClassLoadable()) {
		// return XMLOutputFactory.newFactory(defaultImpl, null as ClassLoader?) is @since Java 6,
		// BUT it cannot be compiled because of an old xml-apis:xml-apis dependency.
		// +--- com.android.tools.build:gradle:7.2.1
		// |    +--- com.android.tools:sdk-common:30.2.1
		// |    |    \--- xerces:xercesImpl:2.12.0
		// |    |         \--- xml-apis:xml-apis:1.4.01
		// It has a very old incompatible version of javax.xml.stream.XMLOutputFactory
		// which doesn't have a "newFactory(String, ClassLoader)" method.
		// I tried to exclude with this but still didn't compile:
		// configurations.compileOnly { exclude("xml-apis", "xml-apis") }
		// So stuck with reflection.
		val newFactory = XMLOutputFactory::class.staticFunctions
			.firstOrNull {
				it.name == "newFactory"
						&& it.parameters.size == 2
						&& it.parameters[0].type.javaType == String::class.java
						&& it.parameters[1].type.javaType == ClassLoader::class.java
			}
		if (newFactory != null) {
			/**
			 * This is a very silly (documented) API,
			 * instead of providing the class name directly (like in other APIs in the same package),
			 * we have to provide a System property.
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
			.single { it.name == "newDefaultInstance" && it.parameters.isEmpty() }
			.call() as TransformerFactory
	}

	val defaultImpl = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"
	if (defaultImpl.isClassLoadable()) {
		return TransformerFactory.newInstance(defaultImpl, null as ClassLoader?)
	}
	// Useful link: https://stackoverflow.com/questions/11314604/how-to-set-saxon-as-the-xslt-processor-in-java
	return TransformerFactory.newInstance()
}

private fun String.isClassLoadable(): Boolean =
	try {
		Class.forName(this)
		/*@return*/ true
	} catch (ignore: ClassNotFoundException) {
		/*@return*/ false
	}
