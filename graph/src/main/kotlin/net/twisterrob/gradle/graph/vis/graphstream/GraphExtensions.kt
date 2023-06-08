@file:JvmName("GraphExtensions")

package net.twisterrob.gradle.graph.vis.graphstream

import org.graphstream.graph.Element
import org.graphstream.graph.Node

fun Iterable<Element>.addClass(clazz: String) {
	for (e in this) {
		e.addClass(clazz)
	}
}

fun Iterable<Element>.removeClass(clazz: String) {
	for (e in this) {
		e.removeClass(clazz)
	}
}

fun Node.addClass(clazz: String): Array<String> =
	(this as Element).addClass(clazz)

fun Element.addClass(clazz: String): Array<String> {
	val classes = this.getClasses()
	classes.add(clazz)
	this.setAttribute("ui.class", classes.joinToString(","))
	return classes.toTypedArray()
}

fun Element.setLabel(label: String) {
	this.setAttribute("ui.label", label)
}

fun Element.getLabel(): String? =
	this.getAttribute("ui.label")

fun Node.removeClass(clazz: String): Array<String> =
	(this as Element).removeClass(clazz)

fun Element.removeClass(clazz: String): Array<String> {
	val classes = this.getClasses()
	classes.remove(clazz)
	if (classes.isEmpty()) {
		this.removeAttribute("ui.class")
	} else {
		this.setAttribute("ui.class", classes.joinToString(","))
	}
	return classes.toTypedArray()
}

fun Element.getClasses(): MutableList<String> {
	val classesString: String? = this.getAttribute("ui.class")
	if (classesString.isNullOrEmpty()) {
		return mutableListOf()
	}
	return classesString.split(",").toMutableList()
}
