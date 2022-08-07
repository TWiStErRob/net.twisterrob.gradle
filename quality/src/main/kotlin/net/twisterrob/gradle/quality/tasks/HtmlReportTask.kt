package net.twisterrob.gradle.quality.tasks

import com.android.utils.SdkUtils
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.compat.conventionCompat
import net.twisterrob.gradle.compat.flatMapCompat
import net.twisterrob.gradle.compat.newInputFileCompat
import net.twisterrob.gradle.compat.newOutputFileCompat
import net.twisterrob.gradle.dsl.reporting
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.bestXMLTransformerFactory
import net.twisterrob.gradle.quality.report.html.produceXml
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.jetbrains.annotations.VisibleForTesting
import java.io.File
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

open class HtmlReportTask : BaseViolationsTask() {

	private val xmlFile: File
		get() = xml.asFile.get()

	@get:OutputFile
	val xml: RegularFileProperty = newOutputFileCompat()

	private val htmlFile: File
		get() = html.asFile.get()

	@get:OutputFile
	val html: RegularFileProperty = newOutputFileCompat()

	private val xslTemplateFile: File?
		get() = xslTemplate.asFile.orNull

	@get:InputFile
	@get:Optional
	val xslTemplate: RegularFileProperty = newInputFileCompat()

	private val xslOutputFile: File
		get() = xsl.asFile.get()

	/**
	 * val xsl: File = xml.parentFile.resolve(xslTemplate.name)
	 */
	// TODO @InputFile as well? maybe separate task? or task steps API?
	@get:OutputFile
	val xsl: RegularFileProperty = newOutputFileCompat()

	init {
		xml.conventionCompat(project.reporting.baseDirectory.file("violations.xml"))
		html.conventionCompat(project.reporting.baseDirectory.file("violations.html"))
		xsl.conventionCompat(
			xml.flatMapCompat { regular ->
				project.layout.file(project.provider {
					regular.asFile.parentFile.resolve(xslTemplateFile?.name ?: "violations.xsl")
				})
			}
		)
		// Setting up this convention would trigger a file not found when no override is set.
		//xslTemplate.conventionCompat(project.layout.projectDirectory.file("config/violations.xsl"))
		@Suppress("LeakingThis")
		doFirst {
			if (xslTemplateFile?.exists() == true) {
				xslTemplateFile!!.copyTo(xslOutputFile, overwrite = true)
			} else {
				val violationsTransformationResource = "/violations.xsl"
				val builtIn = this::class.java.getResourceAsStream(violationsTransformationResource)
					?: error("Cannot find ${violationsTransformationResource} to copy to ${xslOutputFile}.")
				builtIn.use { input ->
					xslOutputFile.outputStream().use { output ->
						input.copyTo(output)
					}
				}
			}
		}
		@Suppress("LeakingThis")
		doLast { transform() }
	}

	override fun processViolations(violations: Grouper.Start<Violations>) {
		project.produceXml(violations, xmlFile, xslOutputFile)
		println("Wrote XML report to ${SdkUtils.fileToUrlString(xmlFile.absoluteFile)}")
	}

	@VisibleForTesting
	internal fun transform() {
		try {
			// We're expecting to get [com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl]
			// Creating [com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl].
			bestXMLTransformerFactory()
				.also { println("XMLAPI::transform.factory: ${it}") }
				.newTransformer(StreamSource(xslOutputFile.reader()))
				.also { println("XMLAPI::transform.transformer: ${it}") }
				.transform(StreamSource(xmlFile.reader()), StreamResult(htmlFile))
			println("Wrote HTML report to ${SdkUtils.fileToUrlString(htmlFile.absoluteFile)}")
		} catch (ex: Throwable) {
			throw GradleException("Cannot transform ${xmlFile}\nto ${htmlFile}\nusing ${xslOutputFile}", ex)
		}
	}
}
