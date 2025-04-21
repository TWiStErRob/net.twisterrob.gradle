@file:JvmName("GraphExtensions")
@file:Suppress("detekt.StringLiteralDuplication")

package net.twisterrob.gradle.graph.vis.graphstream

import org.graphstream.graph.Element
import org.graphstream.graph.Node

var Element.label: String?
	get() = this.getAttribute("ui.label", String::class.java)
	set(value) = this.setAttribute("ui.label", value)

var Element.classes: List<String>
	get() {
		val classesString: String? = this.getAttribute("ui.class", String::class.java)
		if (classesString.isNullOrEmpty()) {
			return mutableListOf()
		}
		return classesString.split(",").toMutableList()
	}
	set(value) {
		if (value.isEmpty()) {
			this.removeAttribute("ui.class")
		} else {
			@Suppress("detekt.SpreadOperator")
			this.setAttribute("ui.class", *value.toTypedArray())
		}
	}

fun Node.addClass(clazz: String) {
	(this as Element).addClass(clazz)
}

fun Element.addClass(clazz: String) {
	this.classes += clazz
}

fun Node.removeClass(clazz: String) {
	(this as Element).removeClass(clazz)
}

fun Element.removeClass(clazz: String) {
	this.classes -= clazz
}
