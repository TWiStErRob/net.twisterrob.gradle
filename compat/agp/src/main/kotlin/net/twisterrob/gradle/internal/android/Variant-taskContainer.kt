package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.Variant
import com.android.build.api.variant.impl.VariantImpl
import com.android.build.gradle.internal.scope.TaskContainer

/**
 * Internal accessor for [VariantImpl.taskContainer] to polyfill old Variant API's access to some task providers.
 */
val Variant.taskContainer: TaskContainer
	get() = unwrapCast<Variant, VariantImpl<*>>().taskContainer
