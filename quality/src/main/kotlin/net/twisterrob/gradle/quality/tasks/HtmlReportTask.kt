package net.twisterrob.gradle.quality.tasks

import com.android.annotations.VisibleForTesting
import net.twisterrob.gradle.quality.report.html.produceXml
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.kotlin.dsl.getByName
import java.io.File
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

open class HtmlReportTask : ValidateViolationsTask() {

	private val xmlFile get() = xml.asFile.get()
	@OutputFile
	var xml: RegularFileProperty = reportDir().file("violations.xml").asProperty()

	private val htmlFile get() = html.asFile.get()
	@OutputFile
	var html: RegularFileProperty = reportDir().file("violations.html").asProperty()

	private val xslTemplateFile: File? get() = xslTemplate.asFile.orNull
	@Suppress("DEPRECATION")
	// keep using layout.fileProperty() instead of objects.fileProperty() for backward compatibility
	@InputFile
	@get:Optional
	var xslTemplate: RegularFileProperty = project.layout.fileProperty().apply {
		//set(project.file("config/violations.xsl"))
	}

	private val xslOutputFile get() = xslOutput.asFile.get()
	/**
	 * val xslOutput: File = xml.parentFile.resolve(xslTemplate.name)
	 */
	// TODO @InputFile as well? maybe separate task? or task steps API?
	@OutputFile
	var xslOutput: RegularFileProperty = xml
		.map { regular ->
			regular.asFileProvider()
				.map { file -> file.parentFile.resolve(xslTemplateFile?.name ?: "violations.xsl") }
				.asRegularFile()

		}
		.asProperty()

	init {
		doFirst {
			if (xslTemplateFile?.exists() == true) {
				xslTemplateFile!!.copyTo(xslOutputFile, overwrite = true)
			} else {
				val builtIn =
					this::class.java.getResourceAsStream("/violations.xsl")
				builtIn.use { input ->
					xslOutputFile.outputStream().use { output ->
						input.copyTo(output)
					}
				}
			}
		}
		action = Action {
			project.produceXml(it, xmlFile, xslOutputFile)
		}
		doLast { transform() }
	}

	@VisibleForTesting
	internal fun transform() {
		try {
			TransformerFactory
				.newInstance()
				.newTransformer(StreamSource(xslOutputFile.reader()))
				.transform(StreamSource(xmlFile.reader()), StreamResult(htmlFile))
		} catch (ex: Throwable) {
			throw GradleException("Cannot transform ${xmlFile}\nto ${htmlFile}\nusing ${xslOutputFile}", ex)
		}
	}

	private fun reportDir(): DirectoryProperty =
		project.extensions
			.getByName<ReportingExtension>(ReportingExtension.NAME)
			.baseDirectory

	@Suppress("DEPRECATION")
	// keep using layout.fileProperty() instead of objects.fileProperty() for backward compatibility
	private fun Provider<RegularFile>.asProperty(): RegularFileProperty =
		project.layout
			.fileProperty()
			.apply { set(this@asProperty) }

	private fun RegularFile.asFileProvider(): Provider<File> =
		project.providers
			.provider { this@asFileProvider }
			.asProperty()
			.asFile

	private fun Provider<File>.asRegularFile(): RegularFile =
		project.layout
			.file(this@asRegularFile)
			.get()
}
