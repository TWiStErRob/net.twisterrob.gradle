package net.twisterrob.gradle.quality.report.html

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import net.twisterrob.gradle.common.grouper.Grouper.Start
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.reflect.jvm.javaMethod

class XmlProducerTest {

	@Test fun `produceXml writes xml`(@TempDir temp: File) {
		val robot = Robot(
			xmlFile = temp.resolve("output.xml"),
			xslFile = temp.resolve("subdir").resolve("style.xsl")
		)
		robot.stubResult(emptyList())
		robot.stubResultIsGrouped(emptyMap())
		robot.stubGroupIsRendered()

		robot.callProduceXml()

		robot.verifyXmlWritten()
	}

	private class Robot(
		private val xmlFile: File,
		private val xslFile: File
	) {
		private lateinit var results: Start<Violations>
		private lateinit var list: List<Violations>
		private lateinit var grouped: Map<Category?, Map<Reporter, List<Violation>>>
		private val projectName: String = "don't matter"

		fun callProduceXml() {
			produceXml(
				results = results,
				projectName = projectName,
				xmlFile = xmlFile.absoluteFile,
				xslFile = xslFile.absoluteFile,
			)
		}

		fun stubResult(list: List<Violations>) {
			this.list = list
			this.results = mockk()
			every { results.list } returns list
		}

		fun stubResultIsGrouped(grouped: Map<Category?, Map<Reporter, List<Violation>>>) {
			this.grouped = grouped
			mockkStatic(::group.javaMethod!!.declaringClass.name)
			every { group(list) } returns grouped
		}

		fun stubGroupIsRendered() {
			mockkStatic(::renderXml.javaMethod!!.declaringClass.name)
			every { renderXml(to = any(), from = grouped, projectName = any(), xslPath = any()) } answers { callOriginal() }
		}

		fun verifyXmlWritten() {
			val xslPath = "${xslFile.parentFile.name}/${xslFile.name}"
			verify { renderXml(to = any(), from = grouped, projectName = projectName, xslPath = xslPath) }
		}
	}
}
