package net.twisterrob.gradle.graph

import net.twisterrob.gradle.logging.JavaPackageHierarchyBasedMap
import net.twisterrob.gradle.logging.RedirectParser
import net.twisterrob.gradle.logging.RedirectingLoggerFactory
import org.slf4j.Logger

internal inline fun <reified T> logger(): Logger =
	createLogger(T::class.java)

internal fun logger(obj: Any): Logger =
	createLogger(obj::class.java)

private fun createLogger(clazz: Class<*>): Logger =
	redirects.getLogger(clazz.name)

private val redirects: RedirectingLoggerFactory by lazy {
	val map = RedirectParser().parse(System.getProperties())
	RedirectingLoggerFactory(JavaPackageHierarchyBasedMap(map)::pickFor)
}
