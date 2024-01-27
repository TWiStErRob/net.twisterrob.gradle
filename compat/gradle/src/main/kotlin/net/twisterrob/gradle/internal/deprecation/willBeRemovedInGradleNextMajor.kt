@file:JvmMultifileClass
@file:JvmName("DeprecationUtils")

package net.twisterrob.gradle.internal.deprecation

import org.gradle.internal.deprecation.DeprecationMessageBuilder
import org.gradle.util.GradleVersion

private typealias Builder<T> = DeprecationMessageBuilder<T>

fun <T : Builder<T>> Builder<T>.willBeRemovedInGradleNextMajor(current: GradleVersion): Builder<T> {
	@Suppress("detekt.MagicNumber")
	val nextMajor = nextMajorVersionNumber(current)
	Builder::class.java
		.getDeclaredMethod("willBeRemovedInGradle$nextMajor")
		.apply { isAccessible = true }
		.invoke(this)
	return this
}
