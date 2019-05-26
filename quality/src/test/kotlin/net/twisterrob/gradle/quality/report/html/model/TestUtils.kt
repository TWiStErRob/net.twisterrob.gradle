package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.gradle.api.Project

internal fun project(path: String): Project = mock<Project>().also {
	whenever(it.path).thenReturn(path)
	whenever(it.name).thenReturn(path.substringAfterLast(":"))
	whenever(it.rootProject).thenReturn(it)
}

internal fun Any.setField(name: String, value: Any?) {
	val field = this::class.java.getDeclaredField(name).apply {
		isAccessible = true
	}
	field.set(this, value)
}

internal inline fun <reified T> JFixture.build(): T = this.create(T::class.java)
