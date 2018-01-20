package net.twisterrob.gradle.common

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet

interface VariantTaskCreator {

	fun applyTo(variants: DomainObjectSet<out BaseVariant>)
}
