package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import org.gradle.api.Project
import java.io.File

internal fun Project.produceXml(results: Grouper.Start<Violations>, xmlFile: File, xslFile: File) {
	val group = group(results.list)
	check(xmlFile.parentFile.let { it.isDirectory || it.mkdirs() }) { "Cannot create parent folder for $xmlFile" }
	check(!xmlFile.exists() || xmlFile.delete()) { "Cannot delete $xmlFile" }
	val xslPath = xslFile.toRelativeString(xmlFile.parentFile).replace('\\', '/')
	xmlFile.writer().use { writer ->
		writer.xmlWriter().use { xmlWriter ->
			renderXml(to = xmlWriter, from = group, projectName = this.rootProject.name, xslPath = xslPath)
		}
	}
}
