package net.twisterrob.gradle.quality.report.html

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import net.twisterrob.gradle.common.grouper.Grouper.Start
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations
import org.gradle.api.Project
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.xml
import java.io.File
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertEquals

class XmlProducerTest {

	@Test fun `produceXml writes xml`() {
		val robot = Robot()
		robot.stubResult(emptyList())
		robot.stubResultIsGrouped(emptyMap())
		robot.stubGroupIsRendered(xml("test"))
		robot.stubWriteXml()
		robot.rootProjectName("don't matter")

		robot.callProduceXml()

		robot.verifyXmlWritten()
	}

	@Test fun `produceXml sets project name in xml`() {
		val robot = Robot()
		robot.stubResult(emptyList())
		robot.stubResultIsGrouped(emptyMap())
		val xmlTree = xml("test")
		robot.stubGroupIsRendered(xmlTree)
		robot.stubWriteXml()
		robot.rootProjectName("root project")

		robot.callProduceXml()

		assertEquals("root project", xmlTree.attributes["project"])
	}

	@Test fun `writeXml writes preamble`(@TempDir temp: File) {
		val output = temp.resolve("output.xml")
		val xsl = temp.resolve("subdir").resolve("style.xsl")

		writeXml(xml("test"), output, xsl)

		assertThat(output, anExistingFile())
		assertEquals(
			"""
				<?xml version="1.0" encoding="utf-8"?>
				<?xml-stylesheet type="text/xsl" href="${xsl.parentFile.name}/${xsl.name}"?>
				<test/>
			""".trimIndent(),
			output.readText()
		)
	}

	private class Robot(
		private val xmlFile: File = File("."),
		private val xslFile: File = File(".")
	) {

		fun callProduceXml() {
			project.produceXml(results, xmlFile, xslFile)
		}

		private lateinit var results: Start<Violations>
		private lateinit var list: List<Violations>
		private lateinit var grouped: Map<Category, Map<Reporter, List<Violation>>>
		private lateinit var xmlTree: Node
		private var project: Project = mockk()

		fun stubResult(list: List<Violations>) {
			this.list = list
			this.results = mockk()
			every { results.list } returns list
		}

		fun stubResultIsGrouped(grouped: Map<Category, Map<Reporter, List<Violation>>>) {
			this.grouped = grouped
			mockkStatic(::group.javaMethod!!.declaringClass.name)
			every { group(list) } returns grouped
		}

		fun stubGroupIsRendered(xmlTree: Node) {
			this.xmlTree = xmlTree
			mockkStatic(::renderXml.javaMethod!!.declaringClass.name)
			every { renderXml(grouped) } returns xmlTree
		}

		fun stubWriteXml() {
			mockkStatic(::writeXml.javaMethod!!.declaringClass.name)
			every { writeXml(xmlTree, xmlFile, xslFile) } just runs
		}

		fun verifyXmlWritten() {
			verify { writeXml(xmlTree, xmlFile, xslFile) }
		}

		fun rootProjectName(name: String) {
			every { project.rootProject } returns mockk rootProject@{
				every { this@rootProject.name } returns name
			}
		}
	}
}
