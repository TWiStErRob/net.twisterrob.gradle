package net.twisterrob.gradle.test

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @param S null or non-null String, declare `val`'s type as necessary
 */
fun <R, S : String?> systemProperty(name: String) = object : ReadWriteProperty<R, S> {
	@Suppress("UNCHECKED_CAST")
	override fun getValue(thisRef: R, property: KProperty<*>): S =
		System.getProperty(name) as S

	override fun setValue(thisRef: R, property: KProperty<*>, value: S) {
		System.setProperty(name, value)
	}
}
