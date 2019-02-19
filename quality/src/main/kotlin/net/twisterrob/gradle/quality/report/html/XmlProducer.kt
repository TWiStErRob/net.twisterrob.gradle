package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import org.gradle.api.Project
import org.redundent.kotlin.xml.Node
import java.io.File

internal fun Project.produceXml(results: Grouper.Start<Violations>, xmlFile: File, xslFile: File) {
	val group = group(results.list)
	val xmlTree = renderXml(group).apply {
		attribute("project", rootProject.name)
	}
	writeXml(xmlTree, xmlFile, xslFile)
}

private fun writeXml(xml: Node, xmlFile: File, xslFile: File) {
	xmlFile.parentFile.mkdirs()
	xmlFile.delete()
	xmlFile.writeText(
		// append processing instructions here as the XML lib doesn't allow to do that
		"""
			|<?xml version="1.0" encoding="utf-8"?>
			|<?xml-stylesheet type="text/xsl" href="${xslFile.relativeTo(xmlFile.parentFile)}"?>
			|${xml.toString(prettyFormat = false)}
		""".trimMargin()
	)
}
