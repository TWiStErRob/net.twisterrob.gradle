package net.twisterrob.gradle.quality.tasks

import com.android.utils.SdkUtils
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.dsl.reporting
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.bestXMLTransformerFactory
import net.twisterrob.gradle.quality.report.html.produceXml
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.jetbrains.annotations.VisibleForTesting
import java.io.File
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

abstract class HtmlReportTask : BaseViolationsTask() {

	private val xmlFile: File
		get() = xml.asFile.get()

	@get:OutputFile
	abstract val xml: RegularFileProperty

	private val htmlFile: File
		get() = html.asFile.get()

	@get:OutputFile
	abstract val html: RegularFileProperty

	private val xslTemplateFile: File?
		get() = xslTemplate.asFile.orNull

	/**
	 * Note: if this is set up in a way that it uses external files, add those to the task inputs.
	 */
	@get:InputFile
	@get:Optional
	@get:PathSensitive(PathSensitivity.NONE)
	abstract val xslTemplate: RegularFileProperty

	private val xslOutputFile: File
		get() = xsl.asFile.get()

	/**
	 * `val xsl: File = xml.parentFile.resolve(xslTemplate.name)` would be better,
	 * but it's not possible to use `xml` in the constructor.
	 */
	// TODO @InputFile as well? maybe separate task? or task steps API?
	@get:OutputFile
	abstract val xsl: RegularFileProperty

	init {
		xml.convention(project.reporting.baseDirectory.file("violations.xml"))
		html.convention(project.reporting.baseDirectory.file("violations.html"))
		xsl.convention(
			xml.flatMap { regular ->
				project.layout.file(project.provider {
					regular.asFile.parentFile.resolve(xslTemplateFile?.name ?: "violations.xsl")
				})
			}
		)
		// Setting up this convention would trigger a file not found when no override is set.
		//xslTemplate.conventionCompat(project.layout.projectDirectory.file("config/violations.xsl"))
		@Suppress("LeakingThis")
		doFirst {
			val xslTemplateFile = xslTemplateFile
			if (xslTemplateFile?.exists() == true) {
				xslTemplateFile.copyTo(xslOutputFile, overwrite = true)
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
		logger.lifecycle("Wrote XML report to ${SdkUtils.fileToUrlString(xmlFile.absoluteFile)}")
	}

	@VisibleForTesting
	internal fun transform() {
		try {
			// We're expecting to get [com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl]
			// Creating [com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl].
			bestXMLTransformerFactory()
				.newTransformer(StreamSource(xslOutputFile.reader()))
				.transform(StreamSource(xmlFile.reader()), StreamResult(htmlFile))
			logger.lifecycle("Wrote HTML report to ${SdkUtils.fileToUrlString(htmlFile.absoluteFile)}")
		} catch (@Suppress("TooGenericExceptionCaught") ex: Throwable) {
			// Slap on more information to the exception.
			throw GradleException("Cannot transform ${xmlFile}\nto ${htmlFile}\nusing ${xslOutputFile}", ex)
		}
	}
}
