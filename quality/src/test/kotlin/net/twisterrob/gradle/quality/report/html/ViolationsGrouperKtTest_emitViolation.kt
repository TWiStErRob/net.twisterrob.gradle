package net.twisterrob.gradle.quality.report.html

import com.flextrade.jfixture.JFixture
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.report.html.model.ViolationViewModel
import org.gradle.api.Project
import org.junit.Test
import org.redundent.kotlin.xml.CDATAElement
import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.xml
import kotlin.test.assertEquals

class ViolationsGrouperKtTest_emitViolation {

	private val fixture = JFixture().apply {
		customise().lazyInstance(Project::class.java) {
			project(":" + build())
		}
	}

	@Test
	fun `message with escapes goes through as is`() {
		val lintMessage = """
			Title
			something with escapes:\n 1:\ 2:\\ 3:\\\ 4:\\\\
		""".trimIndent()

		val result = xml("root") {
			emitViolation(ViolationViewModel.create(fixture.build<Violation>().apply {
				setField("message", lintMessage)
				// make sure message goes through the transformation
				source.setField("reporter", "ANDROIDLINT")
				setField("rule", "IconMissingDensityFolder")
			}))
		}

		val message = result("violation", "details", "message").cdata.text
		assertEquals("""something with escapes:\\n 1:\\ 2:\\\\ 3:\\\\\\ 4:\\\\\\\\""", message)
	}

	@Test
	fun `IconMissingDensityFolder specific message escapes are removed`() {
		val lintMessage = """
			Title
			Missing density variation folders in `src\\main\\res`: drawable-hdpi
		""".trimIndent()

		val result = xml("root") {
			emitViolation(ViolationViewModel.create(fixture.build<Violation>().apply {
				setField("message", lintMessage)
				// make sure message goes through the transformation
				source.setField("reporter", "ANDROIDLINT")
				setField("rule", "IconMissingDensityFolder")
			}))
		}

		val message = result("violation", "details", "message").cdata.text
		assertEquals("""Missing density variation folders in \`src\\main\\res\`: drawable-hdpi""", message)
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
