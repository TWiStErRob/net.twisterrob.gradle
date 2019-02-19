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

internal fun writeXml(xmlTree: Node, xmlFile: File, xslFile: File) {
	check(xmlFile.parentFile.let { it.isDirectory || it.mkdirs() }) { "Cannot create parent folder for $xmlFile" }
	check(!xmlFile.exists() || xmlFile.delete()) { "Cannot delete $xmlFile" }
	// append processing instructions here as the XML lib doesn't allow to do that
	xmlTree.includeXmlProlog = false
	xmlFile.bufferedWriter().use { writer ->
		writer.write("""<?xml version="1.0" encoding="utf-8"?>""" + "\n")
		val xslPath = xslFile.toRelativeString(xmlFile.parentFile).replace('\\', '/')
		writer.write("""<?xml-stylesheet type="text/xsl" href="${xslPath}"?>""" + "\n")
		xmlTree.children.forEach { element ->
			writer.write((element as Node).toString(prettyFormat = false))
		}
	}
}
