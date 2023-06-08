@file:JvmName("GraphExtensions")
@file:Suppress("StringLiteralDuplication")

package net.twisterrob.gradle.graph.vis.graphstream

import org.graphstream.graph.Element
import org.graphstream.graph.Node

val Element.classes: MutableList<String>
	get() {
		val classesString: String? = this.getAttribute("ui.class")
		if (classesString.isNullOrEmpty()) {
			return mutableListOf()
		}
		return classesString.split(",").toMutableList()
	}

fun Node.addClass(clazz: String): Array<String> =
	(this as Element).addClass(clazz)

fun Element.addClass(clazz: String): Array<String> {
	val classes = this.classes
	classes.add(clazz)
	this.setAttribute("ui.class", classes.joinToString(","))
	return classes.toTypedArray()
}

fun Iterable<Element>.addClass(clazz: String) {
	for (e in this) {
		e.addClass(clazz)
	}
}

fun Node.removeClass(clazz: String): Array<String> =
	(this as Element).removeClass(clazz)

fun Element.removeClass(clazz: String): Array<String> {
	val classes = this.classes
	classes.remove(clazz)
	if (classes.isEmpty()) {
		this.removeAttribute("ui.class")
	} else {
		this.setAttribute("ui.class", classes.joinToString(","))
	}
	return classes.toTypedArray()
}

fun Iterable<Element>.removeClass(clazz: String) {
	for (e in this) {
		e.removeClass(clazz)
	}
}

var Element.label: String?
	get() = this.getAttribute("ui.label")
	set(value) = this.setAttribute("ui.label", value)
