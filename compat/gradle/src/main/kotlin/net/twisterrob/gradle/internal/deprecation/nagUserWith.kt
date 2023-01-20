@file:JvmMultifileClass
@file:JvmName("DeprecationUtils")

package net.twisterrob.gradle.internal.deprecation

import org.gradle.internal.deprecation.DeprecationLogger
import org.gradle.internal.deprecation.DeprecationMessageBuilder

fun nagUserWith(builder: DeprecationMessageBuilder<*>, calledFrom: Class<*>) {
	DeprecationLogger::class.java
		.getDeclaredMethod("nagUserWith", DeprecationMessageBuilder::class.java, Class::class.java)
		.apply { isAccessible = true }
		.invoke(null, builder, calledFrom)
}
