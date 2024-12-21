package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import java.io.File

internal fun produceXml(results: Grouper.Start<Violations>, projectName: String, xmlFile: File, xslFile: File) {
	val group = group(results.list)
	check(xmlFile.parentFile.let { it.isDirectory || it.mkdirs() }) { "Cannot create parent folder for $xmlFile" }
	check(!xmlFile.exists() || xmlFile.delete()) { "Cannot delete $xmlFile" }
	val xslPath = xslFile.toRelativeString(xmlFile.parentFile).replace('\\', '/')
	xmlFile.writer().use { writer ->
		writer.xmlWriter().use { xmlWriter ->
			renderXml(to = xmlWriter, from = group, projectName = projectName, xslPath = xslPath)
		}
	}
}
