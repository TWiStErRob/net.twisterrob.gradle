package net.twisterrob.gradle.quality.report.html

import org.redundent.kotlin.xml.CDATAElement
import org.redundent.kotlin.xml.Node

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
