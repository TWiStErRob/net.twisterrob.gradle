package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.Variant
import com.android.build.api.variant.impl.VariantImpl

/**
 * Internal accessor for [VariantImpl.description] to polyfill old Variant API.
 */
val Variant.description: String
	@Suppress("MemberExtensionConflict") // It's fine, that's the point.
	get() = unwrapCast<Variant, VariantImpl<*>>().description
