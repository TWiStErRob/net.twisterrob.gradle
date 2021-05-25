package net.twisterrob.gradle.quality.tasks

import com.android.utils.SdkUtils
import com.google.common.annotations.VisibleForTesting
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.html.produceXml
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.kotlin.dsl.getByName
import java.io.File
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@Suppress("UnstableApiUsage")
abstract class HtmlReportTask : ValidateViolationsTask() {

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

	@get:InputFile
	@get:Optional
	abstract val xslTemplate: RegularFileProperty

	private val xslOutputFile: File
		get() = xslOutput.asFile.get()

	/**
	 * val xslOutput: File = xml.parentFile.resolve(xslTemplate.name)
	 */
	// TODO @InputFile as well? maybe separate task? or task steps API?
	@get:OutputFile
	abstract val xslOutput: RegularFileProperty

	init {
		val reportDir = project.extensions
			.getByName<ReportingExtension>(ReportingExtension.NAME)
			.baseDirectory
		xml.convention(reportDir.file("violations.xml"))
		html.convention(reportDir.file("violations.html"))
		xslOutput.convention(
			xml.flatMap { regular ->
				project.layout.dir(project.provider { regular.asFile.parentFile })
					.map { it.file(xslTemplateFile?.name ?: "violations.xsl") }
			}
		)
		//xslTemplate.convention(project.layout.projectDirectory.file("config/violations.xsl"))
		doFirst {
			if (xslTemplateFile?.exists() == true) {
				xslTemplateFile!!.copyTo(xslOutputFile, overwrite = true)
			} else {
				val builtIn =
					this::class.java.getResourceAsStream("/violations.xsl")!!
				builtIn.use { input ->
					xslOutputFile.outputStream().use { output ->
						input.copyTo(output)
					}
				}
			}
		}
		doLast { transform() }
	}

	override fun processViolations(violations: Grouper.Start<Violations>) {
		project.produceXml(violations, xmlFile, xslOutputFile)
		println("Wrote XML report to ${SdkUtils.fileToUrlString(xmlFile.absoluteFile)}")
	}

	@VisibleForTesting
	internal fun transform() {
		try {
			TransformerFactory
				.newInstance()
				.newTransformer(StreamSource(xslOutputFile.reader()))
				.transform(StreamSource(xmlFile.reader()), StreamResult(htmlFile))
			println("Wrote HTML report to ${SdkUtils.fileToUrlString(htmlFile.absoluteFile)}")
		} catch (ex: Throwable) {
			throw GradleException("Cannot transform ${xmlFile}\nto ${htmlFile}\nusing ${xslOutputFile}", ex)
		}
	}
}
