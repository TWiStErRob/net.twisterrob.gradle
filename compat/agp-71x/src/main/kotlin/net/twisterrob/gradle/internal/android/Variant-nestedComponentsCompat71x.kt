package net.twisterrob.gradle.internal.android

import com.android.build.api.variant.Component
import com.android.build.api.variant.Variant
import org.gradle.api.Incubating

@get:Incubating
val Variant.nestedComponentsCompat71x: List<Component> by Variant::nestedComponents
