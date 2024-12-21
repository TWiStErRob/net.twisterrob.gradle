package net.twisterrob.gradle.quality.tasks

import com.android.utils.SdkUtils
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.dsl.reporting
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.bestXMLTransformerFactory
import net.twisterrob.gradle.quality.report.html.produceXml
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.jetbrains.annotations.VisibleForTesting
import java.io.File
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@CacheableTask
abstract class HtmlReportTask : BaseViolationsTask() {

	@get:OutputFile
	abstract val xml: RegularFileProperty

	private val xmlFile: File
		get() = xml.asFile.get()

	@get:OutputFile
	abstract val html: RegularFileProperty

	private val htmlFile: File
		get() = html.asFile.get()

	/**
	 * Note: if this is set up in a way that it uses external files, add those to the task inputs.
	 */
	@get:InputFile
	@get:Optional
	@get:PathSensitive(PathSensitivity.NONE)
	abstract val xslTemplate: RegularFileProperty

	private val xslTemplateFile: File?
		get() = xslTemplate.asFile.orNull

	// TODO @InputFile as well? maybe separate task? or task steps API?
	@get:OutputFile
	abstract val xsl: RegularFileProperty

	private val xslOutputFile: File
		get() = xsl.asFile.get()

	@get:Input
	abstract val projectName: Property<String>

	init {
		projectName.convention(project.provider { project.rootProject.name })
		@Suppress("LeakingThis")
		xml.convention(project.reporting.baseDirectory.file("violations.xml"))
		@Suppress("LeakingThis")
		html.convention(project.reporting.baseDirectory.file("violations.html"))
		@Suppress("LeakingThis")
		val xslName: Provider<String> = xslTemplate.map { it.asFile.name }.orElse("violations.xsl")
		@Suppress("LeakingThis")
		xsl.convention(project.layout.file(xml.zip(xslName) { xml, xslFileName ->
			xml.asFile.parentFile.resolve(xslFileName)
		}))
		// Setting up this convention would trigger a file not found when no override is set.
		//xslTemplate.conventionCompat(project.layout.projectDirectory.file("config/violations.xsl"))
		@Suppress("LeakingThis")
		doFirst {
			val xslTemplateFile = xslTemplateFile
			if (xslTemplateFile != null) {
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
		produceXml(violations, projectName.get(), xmlFile, xslOutputFile)
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
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Throwable) {
			// Slap on more information to the exception.
			throw GradleException("Cannot transform ${xmlFile}\nto ${htmlFile}\nusing ${xslOutputFile}", ex)
		}
	}
}
