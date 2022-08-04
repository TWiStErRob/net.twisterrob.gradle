package net.twisterrob.gradle.quality.report.html

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.io.StringWriter
import kotlin.test.assertEquals

class ViolationsRendererTest {

	@Test fun `xmlWriter produces a supported writer for test`() {
		val writer = StringWriter().xmlWriter()

		assertEquals("com.ctc.wstx.sw.SimpleNsStreamWriter", writer::class.qualifiedName)
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
}

private fun @receiver:Language("xml") String.unformat(): String =
	trimIndent().lines().joinToString(separator = "")
