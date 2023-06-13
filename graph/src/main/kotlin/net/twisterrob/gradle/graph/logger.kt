package net.twisterrob.gradle.graph

import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal inline fun <reified T> logger(): Logger =
	LoggerFactory.getLogger(T::class.java)

internal fun logger(obj: Any): Logger =
	LoggerFactory.getLogger(obj::class.java)
