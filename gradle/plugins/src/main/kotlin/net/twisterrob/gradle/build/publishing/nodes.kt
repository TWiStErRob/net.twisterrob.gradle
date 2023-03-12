package net.twisterrob.gradle.build.publishing

import groovy.namespace.QName
import groovy.util.Node
import groovy.util.NodeList

private fun Node.getChildren(localName: String): NodeList =
	this.get(localName) as NodeList

private fun Iterable<*>.nodes(): List<Node> =
	this.filterIsInstance<Node>()

/**
 * @see org.gradle.plugins.ear.descriptor.internal.DefaultDeploymentDescriptor.localNameOf
 */
private fun Node.localName(): String =
	if (this.name() is QName) (this.name() as QName).localPart else this.name().toString()

internal fun Node.getChild(localName: String): Node =
	this.getChildren(localName).nodes().singleOrNull()
		?: error("Cannot find $localName in ${this.localName()}: ${this.children().nodes().map { it.localName() }}")
