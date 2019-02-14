package net.twisterrob.gradle.quality.report.html

import com.flextrade.jfixture.JFixture
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violation.Location
import net.twisterrob.gradle.quality.Violation.Severity.ERROR
import net.twisterrob.gradle.quality.Violation.Source
import org.gradle.api.Project
import org.junit.Test
import org.redundent.kotlin.xml.CDATAElement
import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.xml
import java.io.File
import kotlin.test.assertEquals

class ViolationsGrouperKtTest_emitViolation {

	@Test
	fun `message with escapes goes through as is`() {
		val lintMessage = """
			Title
			something with escapes:\n 1:\ 2:\\ 3:\\\ 4:\\\\
		""".trimIndent()

		val result = xml("root") {
			emitViolation(
				Violation(
					"IconMissingDensityFolder", null, ERROR, lintMessage, emptyMap(),
					Location(project(":foo"), "", File("build/test/violation"), 0, 0, 0),
					Source("", "", "ANDROIDLINT", "", File("."), null)
				)
			)
		}

		val message = result("violation", "details", "message").cdata.text
		assertEquals("""something with escapes:\\n 1:\\ 2:\\\\ 3:\\\\\\ 4:\\\\\\\\""", message)
	}

	@Test
	fun `IconMissingDensityFolder specific message escapes are removed`() {
		val originalMessage = """
			Title
			Missing density variation folders in `src\\main\\res`: drawable-hdpi
		""".trimIndent()

		val result = xml("root") {
			emitViolation(
				Violation(
					"IconMissingDensityFolder", null, ERROR, originalMessage, emptyMap(),
					Location(project(":foo"), "", File("build/test/violation"), 0, 0, 0),
					Source("", "", "ANDROIDLINT", "", File("."), null)
				)
			)
		}

		val lintMessage = result("violation", "details", "message").cdata.text
		assertEquals("""Missing density variation folders in \`src\\main\\res\`: drawable-hdpi""", lintMessage)
	}

	private fun project(path: String): Project = mock<Project>().also {
		whenever(it.path).thenReturn(path)
		whenever(it.name).thenReturn(path.substringAfterLast(":"))
		whenever(it.rootProject).thenReturn(it)
	}
}

/**
 * Resolve single child path.
 */
private operator fun Node.invoke(vararg names: String): Node =
	names.fold(this) { node, name -> node.invoke(name) }

/**
 * Resolve only child named `name`.
 */
private operator fun Node.invoke(name: String): Node =
	children.filterIsInstance<Node>().single { it.nodeName == name }

private val Node.cdata: CDATAElement
	get() = children.filterIsInstance<CDATAElement>().single()

private fun Any.setField(name: String, value: Any?) {
	val field = this::class.java.getDeclaredField(name).apply {
		isAccessible = true
	}
	field.set(this, value)
}

private inline fun <reified T> JFixture.build(): T = this.create(T::class.java)
