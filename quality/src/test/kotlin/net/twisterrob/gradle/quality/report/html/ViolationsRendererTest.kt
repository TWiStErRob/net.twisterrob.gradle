package net.twisterrob.gradle.quality.report.html

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.report.html.model.build
import net.twisterrob.gradle.quality.report.html.model.buildProjectPath
import net.twisterrob.gradle.quality.report.html.model.mockProject
import net.twisterrob.gradle.quality.report.html.model.setField
import org.gradle.api.Project
import org.gradle.api.Task
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.io.StringWriter
import kotlin.test.assertEquals

class ViolationsRendererTest {

	private val fixture = JFixture().apply {
		customise().lazyInstance(Project::class.java) {
			mockProject(buildProjectPath())
		}
		customise().lazyInstance(Task::class.java) { mock() }
	}

	@Test fun `xmlWriter produces a supported writer for test`() {
		val writer = StringWriter().xmlWriter()

		val actual = writer::class.qualifiedName

		assertEquals("com.sun.xml.internal.stream.writers.XMLStreamWriterImpl", actual)
	}

	@Test fun `renderXml writes preamble`() {
		val out = StringWriter()
		out.xmlWriter().use { renderXml(it, emptyMap(), "", "some/path/to.xsl") }

		assertEquals(
			"""
				<?xml version="1.0" encoding="utf-8"?>
				<?xml-stylesheet type="text/xsl" href="some/path/to.xsl"?>
				<violations project=""></violations>
			""".unformat(),
			out.toString()
		)
	}

	@Test fun `renderXml writes preamble without stylesheet`() {
		val out = StringWriter()
		out.xmlWriter().use { renderXml(it, emptyMap(), "") }

		assertEquals(
			"""
				<?xml version="1.0" encoding="utf-8"?>
				<violations project=""></violations>
			""".unformat(),
			out.toString()
		)
	}

	@Test fun `renderXml writes project name on root`() {
		val out = StringWriter()
		out.xmlWriter().use { renderXml(it, emptyMap(), "project name") }

		assertEquals(
			"""
				<?xml version="1.0" encoding="utf-8"?>
				<violations project="project name"></violations>
			""".unformat(),
			out.toString()
		)
	}

	@Test fun `renderXml renders a violation`(@TempDir temp: File) {
		val fixtViolation: Violation = fixture.build {
			location.setField("file", temp.resolve(fixture.build<String>()))
			location.setField("startLine", 3)
			location.setField("endLine", 5)
			location.generateTestContent()
			whenever(location.module.relativePath(location.file.parentFile)).thenReturn("relative")
		}
		val problems: Map<Category, Map<Reporter, List<Violation>>> =
			mapOf("test-category" to mapOf("test-reporter" to listOf(fixtViolation)))

		@Language("xml")
		val expected = """
			<?xml version="1.0" encoding="utf-8"?>
			<?xml-stylesheet type="text/xsl" href="some/path/to.xsl"?>
			<violations project="project name">
			<category name="test-category">
			<reporter name="test-reporter">
			<violation>
				<location
					 module="${fixtViolation.location.module.path}"
					 modulePrefix=""
					 moduleName="${fixtViolation.location.module.name}"
					 variant="${fixtViolation.location.variant}"
					 file="${fixtViolation.location.file.absolutePath}"
					 fileName="${fixtViolation.location.file.name}"
					 fileAbsoluteAsUrl="${fixtViolation.location.file.toURI()}"
					 pathRelativeToProject="relative${File.separator}"
					 pathRelativeToModule="relative${File.separator}"
					 fileIsExternal="true"
					 startLine="${fixtViolation.location.startLine}"
					 endLine="${fixtViolation.location.endLine}"
					 column="${fixtViolation.location.column}">
				</location>
				<source
					 parser="${fixtViolation.source.parser}"
					 source="${fixtViolation.source.source}"
					 reporter="${fixtViolation.source.reporter}">
				</source>
				<details
					 rule="${fixtViolation.rule}"
					 category="${fixtViolation.category}"
					 severity="${fixtViolation.severity}">
					<message><![CDATA[${fixtViolation.message}]]></message>
					<context type="code" language="binary"
						 startLine="${fixtViolation.location.startLine - 2}"
						 endLine="${fixtViolation.location.endLine + 2}">
						<![CDATA[
							Line 1\n
							Line 2\n
							Line 3\n
							Line 4\n
							Line 5\n
							Line 6\n
							Line 7
						]]>
					</context>
				</details>
				<specific
					 key="${fixtViolation.specifics.asSequence().elementAt(0).key}"
					 value="${fixtViolation.specifics.asSequence().elementAt(0).value}">
				</specific>
				<specific
					 key="${fixtViolation.specifics.asSequence().elementAt(1).key}"
					 value="${fixtViolation.specifics.asSequence().elementAt(1).value}">
					 
				</specific>
				<specific
					 key="${fixtViolation.specifics.asSequence().elementAt(2).key}"
					 value="${fixtViolation.specifics.asSequence().elementAt(2).value}">
				</specific>
			</violation>
			</reporter>
			</category>
			</violations>
		""".trimIndent()

		val out = StringWriter()
		out.xmlWriter().use { renderXml(it, problems, "project name", "some/path/to.xsl") }

		assertEquals(expected.unformat(), out.toString())
	}
}

private fun Violation.Location.generateTestContent() {
	assertTrue(this.startLine < this.endLine)
	val contents = (1..this.endLine + 10).joinToString("\n") { "Line $it" }
	file.writeText(contents)
}

private fun @receiver:Language("xml") String.unformat(): String =
	lines().joinToString(separator = "") {
		it.trimStart('\t').replace("\\n", System.lineSeparator())
	}
