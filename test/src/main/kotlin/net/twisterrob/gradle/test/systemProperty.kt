package net.twisterrob.gradle.test

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @param R receiver which can be anything, as it's unused.
 * @param S null or non-null [String], declare `val`'s type as necessary. For `var` always use [String].
 * @param name name of the [System.getProperty] to use.
 */
fun <R, S : String?> systemProperty(name: String): ReadWriteProperty<R, S> =
	object : ReadWriteProperty<R, S> {
		@Suppress("UNCHECKED_CAST")
		override fun getValue(thisRef: R, property: KProperty<*>): S =
			System.getProperty(name) as S

		override fun setValue(thisRef: R, property: KProperty<*>, value: S) {
			System.setProperty(name, requireNotNull(value) { "System property ${name} cannot be null." })
		}
	}
